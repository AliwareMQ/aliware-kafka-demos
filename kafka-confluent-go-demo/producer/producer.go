package main

import (
	"encoding/json"
	"fmt"
	"github.com/confluentinc/confluent-kafka-go/kafka"
	"log"
	"os"
	"path/filepath"
	"strconv"
	"time"
)

const (
	INT32_MAX = 2147483647 - 1000
)

type KafkaConfig struct {
	Topic      string `json:"topic"`
	Topic2      string `json:"topic2"`
	GroupId    string `json:"group.id"`
	BootstrapServers    string `json:"bootstrap.servers"`
	SecurityProtocol string `json:"security.protocol"`
	SslCaLocation string `json:"ssl.ca.location"`
	SaslMechanism string `json:"sasl.mechanism"`
	SaslUsername string `json:"sasl.username"`
	SaslPassword string `json:"sasl.password"`
}

// config should be a pointer to structure, if not, panic
func loadJsonConfig() *KafkaConfig {
	workPath, err := os.Getwd()
	if err != nil {
		panic(err)
	}
	configPath := filepath.Join(workPath, "conf")
	fullPath := filepath.Join(configPath, "kafka.json")
	file, err := os.Open(fullPath);
	if (err != nil) {
		msg := fmt.Sprintf("Can not load config at %s. Error: %v", fullPath, err)
		panic(msg)
	}

	defer file.Close()

	decoder := json.NewDecoder(file)
	var config = &KafkaConfig{}
	err = decoder.Decode(config);
	if (err != nil) {
		msg := fmt.Sprintf("Decode json fail for config file at %s. Error: %v", fullPath, err)
		panic(msg)
	}
	json.Marshal(config)
	return  config
}

func doInitProducer(cfg *KafkaConfig) *kafka.Producer {
	fmt.Print("init kafka producer, it may take a few seconds to init the connection\n")
	//common arguments
	var kafkaconf = &kafka.ConfigMap{
		"api.version.request": "true",
		"message.max.bytes": 1000000,
		"linger.ms": 500,
		"sticky.partitioning.linger.ms" : 1000,
		"retries": INT32_MAX,
		"retry.backoff.ms": 1000,
		"acks": "1"}
	kafkaconf.SetKey("bootstrap.servers", cfg.BootstrapServers)

	switch cfg.SecurityProtocol {
	case "PLAINTEXT" :
		kafkaconf.SetKey("security.protocol", "plaintext");
	case "SASL_SSL":
		kafkaconf.SetKey("security.protocol", "sasl_ssl");
		kafkaconf.SetKey("ssl.ca.location", "conf/ca-cert.pem");
		kafkaconf.SetKey("sasl.username", cfg.SaslUsername);
		kafkaconf.SetKey("sasl.password", cfg.SaslPassword);
		kafkaconf.SetKey("sasl.mechanism", cfg.SaslMechanism)
	case "SASL_PLAINTEXT":
		kafkaconf.SetKey("security.protocol", "sasl_plaintext");
		kafkaconf.SetKey("sasl.username", cfg.SaslUsername);
		kafkaconf.SetKey("sasl.password", cfg.SaslPassword);
		kafkaconf.SetKey("sasl.mechanism", cfg.SaslMechanism)
	default:
		panic(kafka.NewError(kafka.ErrUnknownProtocol, "unknown protocol", true))
	}

	producer, err := kafka.NewProducer(kafkaconf)
	if err != nil {
		panic(err)
	}
	fmt.Print("init kafka producer success\n")
	return producer
}

func main() {
	// Choose the correct protocol
	// 9092 for PLAINTEXT
	// 9093 for SASL_SSL, need to provide sasl.username and sasl.password
	// 9094 for SASL_PLAINTEXT, need to provide sasl.username and sasl.password
	cfg := loadJsonConfig();
	producer := doInitProducer(cfg)

    defer producer.Close()

	// Delivery report handler for produced messages
	go func() {
		for e := range producer.Events() {
			switch ev := e.(type) {
			case *kafka.Message:
				if ev.TopicPartition.Error != nil {
					log.Printf("Failed to write access log entry:%v", ev.TopicPartition.Error)
				} else {
					log.Printf("Send OK topic:%v partition:%v offset:%v content:%s\n", *ev.TopicPartition.Topic,  ev.TopicPartition.Partition, ev.TopicPartition.Offset, ev.Value)

				}
			}
		}
	}()

    // Produce messages to topic (asynchronously)
	i := 0
	for {
		i = i + 1
		value := "this is a kafka message from sarama go " + strconv.Itoa(i)
		var msg *kafka.Message = nil
		if i % 2 == 0 {
			msg = &kafka.Message{
				TopicPartition: kafka.TopicPartition{Topic: &cfg.Topic2, Partition: kafka.PartitionAny},
				Value:          []byte(value),
			}
		} else {
			msg = &kafka.Message{
				TopicPartition: kafka.TopicPartition{Topic: &cfg.Topic, Partition: kafka.PartitionAny},
				Value:          []byte(value),
			}
		}
		producer.Produce(msg, nil)
		time.Sleep(time.Duration(1) * time.Millisecond)
	}
	// Wait for message deliveries before shutting down
	producer.Flush(15 * 1000)
}
