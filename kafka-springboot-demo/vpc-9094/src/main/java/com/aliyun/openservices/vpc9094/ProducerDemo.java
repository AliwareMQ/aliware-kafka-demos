package com.aliyun.openservices.vpc9094;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * @author liujiang
 */
@Slf4j
@Component
public class ProducerDemo {

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
