package main

import (
	"encoding/json"
	"fmt"
    "github.com/confluentinc/confluent-kafka-go/kafka"
	"os"
	"path/filepath"
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


func doInitConsumer(cfg *KafkaConfig) *kafka.Consumer {
	fmt.Print("init kafka consumer, it may take a few seconds to init the connection\n")
	//common arguments
	var kafkaconf = &kafka.ConfigMap{
		"api.version.request": "true",
		"auto.offset.reset": "latest",
		"heartbeat.interval.ms": 3000,
		"session.timeout.ms": 30000,
		"max.poll.interval.ms": 120000,
		"fetch.max.bytes": 1024000,
		"max.partition.fetch.bytes": 256000}
	kafkaconf.SetKey("bootstrap.servers", cfg.BootstrapServers);
	kafkaconf.SetKey("group.id", cfg.GroupId)

	switch cfg.SecurityProtocol {
	case "PLAINTEXT" :
		kafkaconf.SetKey("security.protocol", "plaintext");
	case "SASL_SSL":
		kafkaconf.SetKey("security.protocol", "sasl_ssl");
		kafkaconf.SetKey("ssl.ca.location", "./conf/mix-4096-ca-cert");
		kafkaconf.SetKey("sasl.username", cfg.SaslUsername);
		kafkaconf.SetKey("sasl.password", cfg.SaslPassword);
		kafkaconf.SetKey("sasl.mechanism", cfg.SaslMechanism);
        // hostname校验改成空
		kafkaconf.SetKey("ssl.endpoint.identification.algorithm", "None");
		kafkaconf.SetKey("enable.ssl.certificate.verification", "false");
	case "SASL_PLAINTEXT":
		kafkaconf.SetKey("security.protocol", "sasl_plaintext");
		kafkaconf.SetKey("sasl.username", cfg.SaslUsername);
		kafkaconf.SetKey("sasl.password", cfg.SaslPassword);
		kafkaconf.SetKey("sasl.mechanism", cfg.SaslMechanism)

	default:
		panic(kafka.NewError(kafka.ErrUnknownProtocol, "unknown protocol", true))
	}

	consumer, err := kafka.NewConsumer(kafkaconf)
	if err != nil {
		panic(err)
	}
	fmt.Print("init kafka consumer success\n")
	return consumer;
}

func main() {

	// Choose the correct protocol
	// 9092 for PLAINTEXT
	// 9093 for SASL_SSL, need to provide sasl.username and sasl.password
	// 9094 for SASL_PLAINTEXT, need to provide sasl.username and sasl.password
	cfg := loadJsonConfig();
	consumer := doInitConsumer(cfg)

	consumer.SubscribeTopics([]string{cfg.Topic, cfg.Topic2}, nil)

	for {
		msg, err := consumer.ReadMessage(-1)
		if err == nil {
			fmt.Printf("Message on %s: %s\n", msg.TopicPartition, string(msg.Value))
		} else {
			// The client will
			//automatically try to recover from all errors.
			fmt.Printf("Consumer error: %v (%v)\n", err, msg)
		}
	}

	consumer.Close()
}