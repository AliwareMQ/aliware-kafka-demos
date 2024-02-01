package com.aliyun.openservices.kafka.kafka.producer;

import com.aliyun.openservices.kafka.constant.Constants;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.apache.kafka.clients.CommonClientConfigs;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author ：yaxuSong
 * @Description:
 * @Date: 18:36 2018/4/24
 * @Modified by:
 */
@Configuration
public class SenderConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,SenderConfig.class.getClassLoader()
                .getResource(Constants.KAFKA_CLIENT_TRUSTSTORE_JKS).getPath());
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,Constants.KAFKAONSCLIENT);
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,Constants.SASL_SSL);
        props.put(SaslConfigs.SASL_MECHANISM,Constants.SASL_MECHANISM_ONS);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG,16384);
        props.put(ProducerConfig.RETRIES_CONFIG,10);
        props.put(ProducerConfig.LINGER_MS_CONFIG,1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG,33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}