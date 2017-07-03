package com.aliyun.openservices.kafka.ons;

import java.util.Properties;
import java.util.concurrent.Future;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

public class KafkaProducerDemo {

    public static void main(String args[]) {
        if (System.getProperty("java.security.auth.login.config") == null) {
            System.setProperty("java.security.auth.login.config",  KafkaProducerDemo.class.getClassLoader().getResource("kafka_client_jaas.conf").getPath());
        }

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "SASL_SSL://kafka-ons-internet.aliyun.com:8080");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, KafkaProducerDemo.class.getClassLoader().getResource("kafka.client.truststore.jks").getPath());
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "KafkaOnsClient");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "ONS");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

        String message = "This is a message from kafka a";

        String  topic = System.getProperty("kafka.ons.TOPIC", "kafka-ons-test-1");

        Future<RecordMetadata> metadataFuture = producer.send(new ProducerRecord<String, String>(
            topic,
            null,
            System.currentTimeMillis(),
            String.valueOf(message.hashCode()),
            message));
        try {
            RecordMetadata recordMetadata = metadataFuture.get();
            System.out.println("produce ok:" + recordMetadata.toString());
        } catch (Exception e) {
            System.out.println("error occurred");
            e.printStackTrace();
        }
        producer.flush();

    }
}
