package main

import (
	"context"
	"crypto/tls"
	"crypto/x509"
	"encoding/json"
	"github.com/Shopify/sarama"
	"io/ioutil"
	"log"
	"os"
	"os/signal"
	"path/filepath"
	"strings"
	"sync"
	"syscall"
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

func main() {
	log.Println("Starting a new Sarama consumer")

	cfg := loadJsonConfig()
	mqConfig := sarama.NewConfig()
	mqConfig.Version = sarama.V2_2_0_0
	// set to newest in production environment to avoid large amount of duplication
	mqConfig.Consumer.Offsets.Initial = sarama.OffsetNewest

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


	/**
	 * Setup a new Sarama consumer group
	 */
	consumer := Consumer{
		ready: make(chan bool),
	}

	ctx, cancel := context.WithCancel(context.Background())
	client, err := sarama.NewConsumerGroup(strings.Split(cfg.BootstrapServers, ","), cfg.GroupId, mqConfig)
	if err != nil {
		log.Panicf("Error creating consumer group client: %v", err)
	}

	wg := &sync.WaitGroup{}
	wg.Add(1)
	go func() {
		defer wg.Done()
		for {
			if err := client.Consume(ctx, []string{cfg.Topic, cfg.Topic2}, &consumer); err != nil {
				log.Panicf("Error from consumer: %v", err)
			}
			// check if context was cancelled, signaling that the consumer should stop
			if ctx.Err() != nil {
				return
			}
			consumer.ready = make(chan bool)
		}
	}()

	<-consumer.ready // Await till the consumer has been set up
	log.Println("Sarama consumer up and running!...")

	sigterm := make(chan os.Signal, 1)
	signal.Notify(sigterm, syscall.SIGINT, syscall.SIGTERM)
	select {
	case <-ctx.Done():
		log.Println("terminating: context cancelled")
	case <-sigterm:
		log.Println("terminating: via signal")
	}
	cancel()
	wg.Wait()
	if err = client.Close(); err != nil {
		log.Panicf("Error closing client: %v", err)
	}
}

// Consumer represents a Sarama consumer group consumer
type Consumer struct {
	ready chan bool
}

// Setup is run at the beginning of a new session, before ConsumeClaim
func (consumer *Consumer) Setup(sarama.ConsumerGroupSession) error {
	// Mark the consumer as ready
	close(consumer.ready)
	return nil
}

// Cleanup is run at the end of a session, once all ConsumeClaim goroutines have exited
func (consumer *Consumer) Cleanup(sarama.ConsumerGroupSession) error {
	return nil
}

// ConsumeClaim must start a consumer loop of ConsumerGroupClaim's Messages().
func (consumer *Consumer) ConsumeClaim(session sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {

	// NOTE:
	// Do not move the code below to a goroutine.
	// The `ConsumeClaim` itself is called within a goroutine, see:
	// https://github.com/Shopify/sarama/blob/master/consumer_group.go#L27-L29
	for message := range claim.Messages() {
		log.Printf("Message claimed: value = %s, offset = %v, topic = %s, partition =%v", string(message.Value), message.Offset, message.Topic, message.Partition)
		session.MarkMessage(message, "")
	}
	return nil
}

