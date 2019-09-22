package com.aliyun.openservices.kafka.kafka.consumer;


import com.aliyun.openservices.kafka.constant.Constants;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author ：yaxuSong
 * @Description:
 * @Date: 18:15 2018/4/24
 * @Modified by:
 */
@EnableKafka
@Configuration
public class ReceiverConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.group.id}")
    private String groupId;
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,ReceiverConfig.class.getClassLoader()
                .getResource(Constants.KAFKA_CLIENT_TRUSTSTORE_JKS).getPath());
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,Constants.KAFKAONSCLIENT);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, Constants.EARLIEST);
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,Constants.SASL_SSL);
        props.put(SaslConfigs.SASL_MECHANISM,Constants.SASL_MECHANISM_ONS);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,15000);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,1000);
        return props;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

}
