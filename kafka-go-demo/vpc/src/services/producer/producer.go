package main

import (
	"fmt"
	"services"
    "time"
    "strconv"

	"github.com/Shopify/sarama"
)

var cfg *configs.MqConfig
var producer sarama.SyncProducer

func init() {

	fmt.Print("init kafka producer, it may take a few seconds to init the connection\n")

	var err error

	cfg = &configs.MqConfig{}
	configs.LoadJsonConfig(cfg, "kafka.json")

	mqConfig := sarama.NewConfig()

	mqConfig.Producer.Return.Successes = true
    mqConfig.Version=sarama.V0_10_2_1

	if err = mqConfig.Validate(); err != nil {
		msg := fmt.Sprintf("Kafka producer config invalidate. config: %v. err: %v", *cfg, err)
		fmt.Println(msg)
		panic(msg)
	}

	producer, err = sarama.NewSyncProducer(cfg.Servers, mqConfig)
	if err != nil {
		msg := fmt.Sprintf("Kafak producer create fail. err: %v", err)
		fmt.Println(msg)
		panic(msg)
	}

}

func produce(topic string, key string, content string) error {
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
    //the key of the kafka messages 
    //do not set the same the key for all messages, it may cause partition im-balance 
    key := strconv.FormatInt(time.Now().UTC().UnixNano(), 10)
    value := "this is a kafka message!"
	produce(cfg.Topics[0], key, value)
}
