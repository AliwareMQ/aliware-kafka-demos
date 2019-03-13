package com.aliyun.openservices.alikafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaProducerDemo {

    public static void main(String args[]) {
        Properties kafkaProperties = AliKafkaConfigurer.getKafkaProperties();

        Properties props = new Properties();

        // SASL_SSL 相关的设置
        Map<String, String> saslConfig = AliKafkaConfigurer.saslSSLConfig();
        props.putAll(saslConfig);

        //设置接入点，请通过控制台获取对应Topic的接入点
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getProperty("bootstrap.servers"));
        //Kafka消息的序列化方式
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        //请求的最长等待时间
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);

        //构造Producer对象，注意，该对象是线程安全的，一般来说，一个进程内一个Producer对象即可；
        //如果想提高性能，可以多构造几个对象，但不要太多，最好不要超过5个
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

        //构造一个Kafka消息
        String topic = kafkaProperties.getProperty("topic"); //消息所属的Topic，请在控制台申请之后，填写在这里
        String value = "this is the message's value"; //消息的内容

        try {
            for (int i = 0; i < 10; i++) {
                String msg = value + ": " + i;
                ProducerRecord<String, String> kafkaMessage = new ProducerRecord<String, String>(topic, msg);
                //发送消息，并获得一个Future对象
                Future<RecordMetadata> metadataFuture = producer.send(kafkaMessage);
                //同步获得Future对象的结果
                RecordMetadata recordMetadata = metadataFuture.get();
                System.out.println("Produce ok: " + recordMetadata.toString() + " send msg: " + msg);
            }
        } catch (Exception e) {
            //要考虑重试
            //参考常见报错: https://help.aliyun.com/document_detail/68168.html?spm=a2c4g.11186623.6.567.2OMgCB
            System.out.println("error occurred");
            e.printStackTrace();
        }
    }
}
