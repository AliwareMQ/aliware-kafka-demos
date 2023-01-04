package com.aliyun.openservices.vpcssl.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * @author liujiang
 */
@Slf4j
@Component
public class KafkaProducerDemo {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.producer.topic}")
    private String topic;

    public String sendMessage() {
        try {
            //消息的内容
            String value = "this is the message's value";
            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, value);
            SendResult<String, String> result = future.get();
            return "send message success: " + result.getProducerRecord();
        } catch (Exception e) {
            log.error("send message failed.", e);
        }
        return "send message failed.";
    }
}
