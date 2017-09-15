package com.aliyun.openservices.kafka.ons;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaProducerDemo {

    public static void main(String args[]) {
        ApplicationContext context = new  ClassPathXmlApplicationContext("kafka-producer.xml");
        KafkaTemplate kafkaTemplate = context.getBean("KafkaTemplate", KafkaTemplate.class);
        kafkaTemplate.sendDefault(System.currentTimeMillis() + "","hello world");
    }
}
