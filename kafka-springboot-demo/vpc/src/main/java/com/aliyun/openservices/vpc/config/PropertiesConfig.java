package com.aliyun.openservices.vpc.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class PropertiesConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServer;
    @Value("${spring.kafka.config.ssl.endpoint.identification.algorithm}")
    private String sslEndpointIdentificationAlgorithm;

    /**
     * 生产者配置
     */
    @Value("${spring.kafka.producer.max-block-ms}")
    private String kafkaProducerMaxBlockMs;
    @Value("${spring.kafka.producer.retries}")
    private String kafkaProducerRetries;
    @Value("${spring.kafka.producer.reconnect-backoff-ms}")
    private String kafkaProducerReconnectBackoffMs;


    /**
     * 消费组配置
     */
    @Value("${spring.kafka.consumer.group-id}")
    private String kafkaConsumerGroupId;
    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private Boolean kafkaConsumerAutoCommit;
    @Value("${spring.kafka.consumer.auto-commit-interval}")
    private Integer kafkaConsumerAutoCommitInterval;
    @Value("${spring.kafka.consumer.max-poll-records}")
    private Integer kafkaConsumerMaxPollRecords;
    @Value("${spring.kafka.consumer.max-partition-fetch-bytes}")
    private Integer kafkaConsumerMaxPartitionFetchBytes;
    @Value("${spring.kafka.consumer.fetch-max-bytes}")
    private Integer kafkaConsumerFetchMaxBytes;
    @Value("${spring.kafka.consumer.session-timeout}")
    private String kafkaConsumerSessionTimeout;
    @Value("${spring.kafka.listener.batch-listener}")
    private Boolean kafkaConsumerBatchListener;
    @Value("${spring.kafka.listener.concurrency}")
    private Integer kafkaConsumerConcurrency;


}
