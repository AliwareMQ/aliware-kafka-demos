package main

import (
	"fmt"
	"os"
	"services"
	"os/signal"
	"github.com/Shopify/sarama"
	"github.com/bsm/sarama-cluster"
)

var cfg *configs.MqConfig
var consumer *cluster.Consumer
var sig chan os.Signal

func init() {
	fmt.Println("init kafka consumer, it may take a few seconds...")

	var err error

	cfg := &configs.MqConfig{}
	configs.LoadJsonConfig(cfg, "kafka.json")

	clusterCfg := cluster.NewConfig()

	clusterCfg.Consumer.Return.Errors = true
	clusterCfg.Consumer.Offsets.Initial = sarama.OffsetOldest
	clusterCfg.Group.Return.Notifications = true

	clusterCfg.Version = sarama.V0_10_2_2
	if err = clusterCfg.Validate(); err != nil {
		msg := fmt.Sprintf("Kafka consumer config invalidate. config: %v. err: %v", *clusterCfg, err)
		fmt.Println(msg)
		panic(msg)
	}

	consumer, err = cluster.NewConsumer(cfg.Servers, cfg.ConsumerId, cfg.Topics, clusterCfg)
	if err != nil {
		msg := fmt.Sprintf("Create kafka consumer error: %v. config: %v", err, clusterCfg)
		fmt.Println(msg)
		panic(msg)
	}

	sig = make(chan os.Signal, 1)

}

func Start() {
	go consume()
}

func consume() {
	for {
		select {
		case msg, more := <-consumer.Messages():
			if more {
                fmt.Printf("Partition:%d, Offset:%d, Key:%s, Value:%s Timestamp:%s\n", msg.Partition, msg.Offset, string(msg.Key), string(msg.Value), msg.Timestamp)
				consumer.MarkOffset(msg, "") // mark message as processed
			}
		case err, more := <-consumer.Errors():
			if more {
				fmt.Println("Kafka consumer error: %v", err.Error())
			}
		case ntf, more := <-consumer.Notifications():
			if more {
				fmt.Println("Kafka consumer rebalance: %v", ntf)
			}
		case <-sig:
			fmt.Errorf("Stop consumer server...")
			consumer.Close()
			return
		}
	}

}

func Stop(s os.Signal) {
	fmt.Println("Recived kafka consumer stop signal...")
	sig <- s
	fmt.Println("kafka consumer stopped!!!")
}

func main() {

	signals := make(chan os.Signal, 1)
	signal.Notify(signals, os.Interrupt)

	Start()

	select {
	case s := <-signals:
		Stop(s)
	}

}
