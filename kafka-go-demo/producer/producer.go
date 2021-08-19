package main

import (
	"crypto/tls"
	"crypto/x509"
	"encoding/json"
	"github.com/Shopify/sarama"
	"hash"
	"hash/fnv"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"
)

type KafkaConfig struct {
	Topic      string `json:"topic"`
	Topic2      string `json:"topic2"`
	GroupId    string `json:"group.id"`
	BootstrapServers    string `json:"bootstrap.servers"`
	SecurityProtocol string `json:"security.protocol"`
	SaslMechanism string `json:"sasl.mechanism"`
	SaslUsername string `json:"sasl.username"`
	SaslPassword string `json:"sasl.password"`
}


func getFullPath(file string) string {
	workPath, err := os.Getwd()
	if err != nil {
		panic(err)
	}
	configPath := filepath.Join(workPath, "conf")
	fullPath := filepath.Join(configPath, file)
	return fullPath
}


// config should be a pointer to structure, if not, panic
func loadJsonConfig() *KafkaConfig {
	fullPath := getFullPath("kafka.json")
	file, err := os.Open(fullPath)
	if err != nil {
		log.Panicf("Can not load config at %s. Error: %v", fullPath, err)
	}

	defer file.Close()

	decoder := json.NewDecoder(file)
	var config = &KafkaConfig{}
	err = decoder.Decode(config)
	if err != nil {
		log.Panicf("Decode json fail for config file at %s. Error: %v", fullPath, err)
	}
	json.Marshal(config)
	return  config
}

//implement the function pointer
// type PartitionerConstructor func(topic string) Partitioner
func NewStickyPartitioner(topic string) sarama.Partitioner {
	p := new(stickyPartitioner)
	p.hasher = fnv.New32a()
	p.referenceAbs = false
	p.stickSize = 32 * 1024
	p.totalSize = 0
	return p
}

type stickyPartitioner struct {
	hasher       hash.Hash32
	referenceAbs bool
	stickSize int32
	totalSize int32
}

func (p *stickyPartitioner) Partition(message *sarama.ProducerMessage, numPartitions int32) (int32, error) {
	if message.Key == nil {
		if message.Value != nil {
			p.totalSize += int32(message.Value.Length())
		}
		//prevent overflow
		if p.totalSize > p.stickSize * numPartitions {
			p.totalSize = p.totalSize - p.stickSize * numPartitions
		}
		//double check overflow
		if p.totalSize < 0 {
			p.totalSize = 0
		}
		return (p.totalSize / p.stickSize) % numPartitions, nil
	}
	bytes, err := message.Key.Encode()
	if err != nil {
		return -1, err
	}
	p.hasher.Reset()
	_, err = p.hasher.Write(bytes)
	if err != nil {
		return -1, err
	}
	var partition int32
	// Turns out we were doing our absolute value in a subtly different way from the upstream
	// implementation, but now we need to maintain backwards compat for people who started using
	// the old version; if referenceAbs is set we are compatible with the reference java client
	// but not past Sarama versions
	if p.referenceAbs {
		partition = (int32(p.hasher.Sum32()) & 0x7fffffff) % numPartitions
	} else {
		partition = int32(p.hasher.Sum32()) % numPartitions
		if partition < 0 {
			partition = -partition
		}
	}
	return partition, nil
}

func (p *stickyPartitioner) RequiresConsistency() bool {
	return true
}

func (p *stickyPartitioner) MessageRequiresConsistency(message *sarama.ProducerMessage) bool {
	return message.Key != nil
}

func main() {

	log.Println("Starting a new Sarama producer")

	cfg := loadJsonConfig()
	mqConfig := sarama.NewConfig()
	mqConfig.Version = sarama.V2_2_0_0
	mqConfig.Producer.Return.Successes = true
	mqConfig.Producer.Partitioner = NewStickyPartitioner

	mqConfig.Producer.RequiredAcks = 1
	mqConfig.Producer.Flush.Frequency = 500 * time.Millisecond // Flush batches every 500ms
	mqConfig.Producer.Flush.Bytes = 32 * 1024

	mqConfig.Producer.MaxMessageBytes = 1000 * 1000
	mqConfig.Metadata.RefreshFrequency = 5 * time.Minute

	//The meaning of max is not equal to JVM Producer, cannot set the max retry to  INT_MAX = int(^uint(0) >> 1)
	mqConfig.Producer.Retry.Max = 30
	mqConfig.Producer.Retry.Backoff = time.Second


	switch cfg.SecurityProtocol {
	case "plaintext" :
		//do nothing
	case "sasl_ssl":
		mqConfig.Net.SASL.Enable = true
		mqConfig.Net.SASL.User = cfg.SaslUsername
		mqConfig.Net.SASL.Password = cfg.SaslPassword
		mqConfig.Net.SASL.Handshake = true

		certBytes, err := ioutil.ReadFile(getFullPath("ca-cert"))
		if err != nil {
			log.Panicf("kafka client read cert file failed %v", err)
		}
		clientCertPool := x509.NewCertPool()
		ok := clientCertPool.AppendCertsFromPEM(certBytes)
		if !ok {
			log.Panicf("kafka client failed to parse root certificate")
		}
		mqConfig.Net.TLS.Config = &tls.Config{
			RootCAs:            clientCertPool,
			InsecureSkipVerify: true,
		}
		mqConfig.Net.TLS.Enable = true
	case "sasl_plaintext":
		mqConfig.Net.SASL.Enable = true
		mqConfig.Net.SASL.User = cfg.SaslUsername
		mqConfig.Net.SASL.Password = cfg.SaslPassword
		mqConfig.Net.SASL.Handshake = true
	default:
		log.Panicf("unknown protocol %v", cfg.SecurityProtocol)
	}

	if err := mqConfig.Validate(); err != nil {
		log.Panicf("Kafka producer config invalidate. config: %v. err: %v", *cfg, err)
	}

	producer, err := sarama.NewAsyncProducer(strings.Split(cfg.BootstrapServers, ","), mqConfig)
	if err != nil {
		log.Panicf("Kafak producer create fail. err: %v", err)
	}

	//the key of the kafka messages
    //do not set the same the key for all messages, it may cause partition im-balance
	go func() {
		for {
			select {
			case producerError := <-producer.Errors():
				log.Panicf("Failed to write access log entry:%v", producerError)
			case message := <- producer.Successes():
				log.Printf("Send OK topic:%s partition:%v offset:%v content:%s\n", message.Topic,  message.Partition, message.Offset, message.Value)
			}
		}
	}()

	i := 0
	for {
		i = i + 1
		value := "this is a kafka message from sarama go " + strconv.Itoa(i)
		msg := &sarama.ProducerMessage{
			Value: sarama.StringEncoder(value),
			Timestamp: time.Now(),
		}
		if i % 2 == 0 {
			msg.Topic = cfg.Topic2
		} else {
			msg.Topic = cfg.Topic
		}
		producer.Input() <- msg
		time.Sleep(time.Duration(1) * time.Millisecond)
	}
}
