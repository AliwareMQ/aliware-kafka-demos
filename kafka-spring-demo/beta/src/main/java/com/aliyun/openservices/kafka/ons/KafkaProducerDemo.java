package com.aliyun.openservices.kafka.ons;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

public class KafkaProducerDemo {

    public static void main(String args[]) throws Exception {
        ApplicationContext context = new  ClassPathXmlApplicationContext("kafka-producer.xml");
        KafkaTemplate kafkaTemplate = context.getBean("KafkaTemplate", KafkaTemplate.class);
        //不指定Topic发送，会把消息发往默认配置的Topic
        ListenableFuture<SendResult> future = kafkaTemplate.sendDefault(System.currentTimeMillis() + "","hello world");
        //如果需要同步获得结果，记得调用get
        future.get();
        //也可以直接指定Topic进行发送
        //ListenableFuture<SendResult> future = kafkaTemplate.send("Topic"， System.currentTimeMillis() + "","hello world");
    }
}
