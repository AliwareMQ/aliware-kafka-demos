package main

import (
	"fmt"
    "kafkagodemo/configs"
    "github.com/confluentinc/confluent-kafka-go/kafka"
)

var cfg *configs.KafkaConfig
var producer  *kafka.Producer

func doInit() {
	fmt.Print("init kafka producer, it may take a few seconds to init the connection\n")

	var err error

	cfg = &configs.KafkaConfig{}
	configs.LoadJsonConfig(cfg, "kafka.json")


    producer, err = kafka.NewProducer(&kafka.ConfigMap{
		"api.version.request": "true",
		"bootstrap.servers":  cfg.Servers,
        "message.max.bytes": 1000000,
        "security.protocol": "plaintext",
        "linger.ms": 10,
        "retries": 30,
        "retry.backoff.ms": 1000,
        "acks": "1"})
	if err != nil {
		panic(err)
	}

}

func main() {
	//Do it once
	doInit()

    defer producer.Close()

	// Delivery report handler for produced messages
	go func() {
		for e := range producer.Events() {
			switch ev := e.(type) {
			case *kafka.Message:
				if ev.TopicPartition.Error != nil {
					fmt.Printf("Delivery failed: %v\n", ev.TopicPartition)
				} else {
					fmt.Printf("Delivered message to %v\n", ev.TopicPartition)
				}
			}
		}
	}()

    // Produce messages to topic (asynchronously)
	topic := cfg.Topic
	for _, word := range []string{"Welcome", "to", "the", "Confluent", "Kafka", "Golang", "client"} {
		producer.Produce(&kafka.Message{
			TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
			Value:          []byte(word),
		}, nil)
	}

	// Wait for message deliveries before shutting down
	producer.Flush(15 * 1000)
}
