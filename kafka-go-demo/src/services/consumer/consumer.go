package main

import (
	"crypto/tls"
	"crypto/x509"
	"fmt"
	"io/ioutil"
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
	fmt.Println("init kafka consumer")

	var err error

	cfg := &configs.MqConfig{}
	configs.LoadJsonConfig(cfg, "mq.json")

	clusterCfg := cluster.NewConfig()

	clusterCfg.Net.SASL.Enable = true
	clusterCfg.Net.SASL.User = cfg.Ak
	clusterCfg.Net.SASL.Password = cfg.Password
	clusterCfg.Net.SASL.Handshake = true

	certBytes, err := ioutil.ReadFile(configs.GetFullPath(cfg.CertFile))
	clientCertPool := x509.NewCertPool()
	ok := clientCertPool.AppendCertsFromPEM(certBytes)
	if !ok {
		panic("kafka consumer failed to parse root certificate")
	}

	clusterCfg.Net.TLS.Config = &tls.Config{
		//Certificates:       []tls.Certificate{},
		RootCAs:            clientCertPool,
		InsecureSkipVerify: true,
	}

	clusterCfg.Net.TLS.Enable = true
	clusterCfg.Consumer.Return.Errors = true
	clusterCfg.Consumer.Offsets.Initial = sarama.OffsetOldest
	clusterCfg.Group.Return.Notifications = true

	clusterCfg.Version = sarama.V0_10_0_0
	if err = clusterCfg.Validate(); err != nil {
		log.Panicf("Kafka consumer config invalidate. config: %v. err: %v", *clusterCfg, err)
	}

	consumer, err = cluster.NewConsumer(cfg.Servers, cfg.ConsumerId, cfg.Topics, clusterCfg)
	if err != nil {
		log.Panicf("Create kafka consumer error: %v. config: %v", err, clusterCfg)
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

				fmt.Printf("kafka consumer msg: %v\n", *msg)
				consumer.MarkOffset(msg, "") // mark message as processed
			}
		case err, more := <-consumer.Errors():
			if more {
				fmt.Printf("Kafka consumer error: %v\n", err.Error())
			}
		case ntf, more := <-consumer.Notifications():
			if more {
				fmt.Printf("Kafka consumer rebalance: %v\n", ntf)
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
