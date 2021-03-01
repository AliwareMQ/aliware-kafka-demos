package main

import (
	"fmt"
    "kafkagodemo/configs"
    "github.com/confluentinc/confluent-kafka-go/kafka"
)

var cfg *configs.KafkaConfig
var consumer  *kafka.Consumer

func doInit() {

	fmt.Print("init kafka producer, it may take a few seconds to init the connection\n")

	var err error

	cfg = &configs.KafkaConfig{}
	configs.LoadJsonConfig(cfg, "kafka.json")


    consumer, err = kafka.NewConsumer(&kafka.ConfigMap{
		"bootstrap.servers": cfg.Servers,
		"group.id":          cfg.ConsumerId,
		"auto.offset.reset": "latest",
		"api.version.request": "true",
		"heartbeat.interval.ms": 3000,
		"session.timeout.ms": 30000,
		"max.poll.interval.ms": 120000,
		"fetch.max.bytes": 1024000,
		"max.partition.fetch.bytes": 256000,
	})
	if err != nil {
		panic(err)
	}

}


func main() {

	//Init it once
	doInit()

	consumer.SubscribeTopics([]string{cfg.Topic}, nil)

	for {
		msg, err := consumer.ReadMessage(-1)
		if err == nil {
			fmt.Printf("Message on %s: %s\n", msg.TopicPartition, string(msg.Value))
		} else {
			// The client will automatically try to recover from all errors.
			fmt.Printf("Consumer error: %v (%v)\n", err, msg)
		}
	}

	consumer.Close()
}