package main

import (
	"crypto/tls"
	"crypto/x509"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"github.com/Shopify/sarama"
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

func produce(producer sarama.SyncProducer, topic string, key string, content string) error {
	msg := &sarama.ProducerMessage{
		Topic: topic,
		Key:   sarama.StringEncoder(key),
		Value: sarama.StringEncoder(content),
        Timestamp: time.Now(),
	}

	_, _, err := producer.SendMessage(msg)
	if err != nil {
		msg := fmt.Sprintf("Send Error topic: %v. key: %v. content: %v", topic, key, content)
		fmt.Println(msg)
		return err
	}
    fmt.Printf("Send OK topic:%s key:%s value:%s\n", topic, key, content)

	return nil
}

func main() {

	log.Println("Starting a new Sarama consumer")

	cfg := loadJsonConfig()
	mqConfig := sarama.NewConfig()
	mqConfig.Version = sarama.V2_2_0_0
	mqConfig.Producer.Return.Successes = true
	mqConfig.Producer.Partitioner = NewStickyPartitioner


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

	producer, err := sarama.NewSyncProducer(strings.Split(cfg.BootstrapServers, ","), mqConfig)
	if err != nil {
		log.Panicf("Kafak producer create fail. err: %v", err)
	}

	//the key of the kafka messages
    //do not set the same the key for all messages, it may cause partition im-balance
	i := 0
	for {
		key := strconv.FormatInt(time.Now().UTC().UnixNano(), 10)
		i = i + 1
		value := "this is a kafka message from sarama go " + strconv.Itoa(i)
		if i % 2 == 0 {
			produce(producer, cfg.Topic2, key, value)
		} else {
			produce(producer, cfg.Topic, key, value)
		}
		time.Sleep(time.Duration(1) * time.Millisecond)
	}
}
