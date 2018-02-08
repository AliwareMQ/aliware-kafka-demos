package com.aliyun.openservices.kafka.ons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

public class KafkaConsumerDemo {

    public static void main(String args[]) {
        if (System.getProperty("java.security.auth.login.config") == null) {
            System.setProperty("java.security.auth.login.config",  KafkaConsumerDemo.class.getClassLoader().getResource("kafka_client_jaas.conf").getPath());
        }
        String  topic = System.getProperty("kafka.ons.TOPIC", "kafka-ons-test-1");

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "SASL_SSL://kafka-ons-internet.aliyun.com:8080");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, KafkaConsumerDemo.class.getClassLoader().getResource("kafka.client.truststore.jks").getPath());
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "KafkaOnsClient");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "ONS");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, System.getProperty("kafka.ons.CID","kafka-test-group"));
        KafkaConsumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<String, String>(props);
        List<String> subscribedTopics =  new ArrayList<String>();
        //如果需要订阅多个Topic，则在这里add进去即可
        //subscribedTopics.add(topic1);
        subscribedTopics.add(topic);
        consumer.subscribe(subscribedTopics);
        while (true){
            ConsumerRecords<String, String> records = consumer.poll(200);
            for (ConsumerRecord<String, String> record : records) {
                System.out.println(String.format("Consume partition:%d offset:%d", record.partition(), record.offset()));
            }
        }
    }
}
