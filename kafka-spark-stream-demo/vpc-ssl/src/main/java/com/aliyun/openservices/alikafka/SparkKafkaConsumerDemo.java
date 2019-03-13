package com.aliyun.openservices.alikafka;

import java.util.*;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import scala.Tuple2;


public class SparkKafkaConsumerDemo {

    public static void main(String args[]) throws InterruptedException {
        Properties kafkaProperties = AliKafkaConfigurer.getKafkaProperties();

        Map<String, Object> kafkaParams = new HashMap<String, Object>();

        // SASL_SSL 相关的设置
        Map<String, String> saslConfig = AliKafkaConfigurer.saslSSLConfig();
        kafkaParams.putAll(saslConfig);

        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getProperty("bootstrap.servers"));
        kafkaParams.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        kafkaParams.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        kafkaParams.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getProperty("group.id"));


        SparkConf conf = new SparkConf().setAppName("SparkKafkaConsumerDemo");
        conf.setMaster("local[*]"); //spark.master
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(60));
        JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(jssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(Collections.singletonList(kafkaProperties.getProperty("topic")), kafkaParams));
        JavaPairDStream<String, String> NglogCounts = stream.mapToPair(record -> new Tuple2<String, String>(record.key(), record.value()));
        NglogCounts.print();
        jssc.start();
        jssc.awaitTermination();
    }
}
