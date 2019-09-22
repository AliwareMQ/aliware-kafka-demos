package com.aliyun.openservices.kafka.kafka.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @Author ：yaxuSong
 * @Description:
 * @Date: 17:08 2018/4/24
 * @Modified by:
 */
@Service
public class Sender {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.topic.test}")
    private String topic;

    private static final Logger log = LoggerFactory.getLogger(Sender.class);

    public void send(String message){
        log.info("sending message='{}' to topic='{}'", message, topic);
        kafkaTemplate.send(topic, message);
    }
}
