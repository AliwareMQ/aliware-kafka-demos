package main

import (
	"crypto/tls"
	"crypto/x509"
	"encoding/json"
	"github.com/Shopify/sarama"
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

func generateKafkaConf(cfg *KafkaConfig)  *sarama.Config{
	kafkaConf := sarama.NewConfig()
	kafkaConf.Version = sarama.V2_2_0_0
	kafkaConf.Producer.Return.Successes = true
	kafkaConf.Producer.Partitioner = NewStickyPartitioner

	kafkaConf.Producer.RequiredAcks = 1
	kafkaConf.Producer.Flush.Frequency = 500 * time.Millisecond // Flush batches every 500ms
	kafkaConf.Producer.Flush.Bytes = 32 * 1024

	kafkaConf.Producer.MaxMessageBytes = 1000 * 1000
	kafkaConf.Metadata.RefreshFrequency = 5 * time.Minute

	//The meaning of max is not equal to JVM Producer, cannot set the max retry to  INT_MAX = int(^uint(0) >> 1)
	kafkaConf.Producer.Retry.Max = 30
	kafkaConf.Producer.Retry.Backoff = time.Second

	switch cfg.SecurityProtocol {
	case "PLAINTEXT" :
		//do nothing
	case "SASL_SSL":
		kafkaConf.Net.SASL.Enable = true
		kafkaConf.Net.SASL.User = cfg.SaslUsername
		kafkaConf.Net.SASL.Password = cfg.SaslPassword
		kafkaConf.Net.SASL.Handshake = true

		certBytes, err := ioutil.ReadFile(getFullPath("mix-4096-ca-cert"))
		if err != nil {
			log.Panicf("kafka client read cert file failed %v", err)
		}
		clientCertPool := x509.NewCertPool()
		ok := clientCertPool.AppendCertsFromPEM(certBytes)
		if !ok {
			log.Panicf("kafka client failed to parse root certificate")
		}
		kafkaConf.Net.TLS.Config = &tls.Config{
			RootCAs:            clientCertPool,
			InsecureSkipVerify: true,
		}
		kafkaConf.Net.TLS.Enable = true
	case "SASL_PLAINTEXT":
		kafkaConf.Net.SASL.Enable = true
		kafkaConf.Net.SASL.User = cfg.SaslUsername
		kafkaConf.Net.SASL.Password = cfg.SaslPassword
		kafkaConf.Net.SASL.Handshake = true
	default:
		log.Panicf("unknown protocol %v", cfg.SecurityProtocol)
	}

	switch cfg.SaslMechanism {
	case "PLAIN":
		//do nothing
	case "SCRAM-SHA-256":
		kafkaConf.Net.SASL.SCRAMClientGeneratorFunc = func() sarama.SCRAMClient { return &XDGSCRAMClient{HashGeneratorFcn: SHA256} }
		kafkaConf.Net.SASL.Mechanism = sarama.SASLMechanism(sarama.SASLTypeSCRAMSHA256)
	case "SCRAM-SHA-512":
		kafkaConf.Net.SASL.SCRAMClientGeneratorFunc = func() sarama.SCRAMClient { return &XDGSCRAMClient{HashGeneratorFcn: SHA512} }
		kafkaConf.Net.SASL.Mechanism = sarama.SASLMechanism(sarama.SASLTypeSCRAMSHA512)
	default:
		log.Fatalf("invalid SHA algorithm \"%s\": can be either \"SCRAM-SHA-256\" or \"SCRAM-SHA-512\"", cfg.SaslMechanism)
	}

	if err := kafkaConf.Validate(); err != nil {
		log.Panicf("Kafka producer config invalidate. config: %v. err: %v", *cfg, err)
	}
	return kafkaConf
}



func main() {

	log.Println("Starting a new Sarama producer")

	cfg := loadJsonConfig()
	kafkaConf := generateKafkaConf(cfg)
	producer, err := sarama.NewAsyncProducer(strings.Split(cfg.BootstrapServers, ","), kafkaConf)
	if err != nil {
		log.Panicf("Kafak producer create fail. err: %v", err)
	}

	//the key of the kafka messages
    //do not set the same the key for all messages, it may cause partition im-balance
	go func() {
		for {
			select {
			case producerError := <-producer.Errors():
				log.Printf("Failed to write access log entry:%v", producerError)
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
