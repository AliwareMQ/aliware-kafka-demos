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
	// set to newest in production environment to avoid large amount of duplication
	kafkaConf.Metadata.RefreshFrequency = 5 * time.Minute
	kafkaConf.Consumer.Offsets.Initial = sarama.OffsetNewest
	//Set to 5 minutes, align with jvm consumer
	kafkaConf.Consumer.Group.Rebalance.Timeout = 	5 * 60 * time.Second


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
	log.Println("Starting a new Sarama consumer")

	cfg := loadJsonConfig()

	kafkaConf := generateKafkaConf(cfg)
	/**
	 * Setup a new Sarama consumer group
	 */
	consumer := Consumer{
		ready: make(chan bool),
	}

	ctx, cancel := context.WithCancel(context.Background())
	client, err := sarama.NewConsumerGroup(strings.Split(cfg.BootstrapServers, ","), cfg.GroupId, kafkaConf)
	if err != nil {
		log.Panicf("Error creating consumer group client: %v", err)
	}

	wg := &sync.WaitGroup{}
	wg.Add(1)
	go func() {
		defer wg.Done()
		for {
			if err := client.Consume(ctx, []string{cfg.Topic, cfg.Topic2}, &consumer); err != nil {
				log.Printf("Error from consumer: %v", err)
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

