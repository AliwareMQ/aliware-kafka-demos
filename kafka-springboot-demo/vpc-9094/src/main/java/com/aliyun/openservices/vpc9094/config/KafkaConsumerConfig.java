package com.aliyun.openservices.vpc9094.config;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka消费端配置
 *
 * @author liujiang
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Autowired
    private PropertiesConfig propertiesConfig;

    /**
     * 配置监听，将消费工厂信息配置进去
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // 多线程消费
        factory.setConcurrency(propertiesConfig.getKafkaConsumerConcurrency());
        return factory;
    }

    /**
     * 消费消费工厂
     */
    @Bean
    public ConsumerFactory<Integer, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * 消费配置
     */
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>(15);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, propertiesConfig.getKafkaBootstrapServer());
        // Kafka 消息的序列化方式
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        props.put(ConsumerConfig.GROUP_ID_CONFIG, propertiesConfig.getKafkaConsumerGroupId());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, propertiesConfig.getKafkaConsumerAutoCommit());
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, propertiesConfig.getKafkaConsumerAutoCommitInterval());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, propertiesConfig.getKafkaConsumerSessionTimeout());
        //hostname校验改成空
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, propertiesConfig.getSslEndpointIdentificationAlgorithm());
        // sasl 认证
        KafkaSaslConfig.kafkaSaslConfig(props, propertiesConfig);
        return props;
    }
}