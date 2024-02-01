package com.aliyun.openservices.kafka.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * @Author ：yaxuSong
 * @Description:
 * @Date: 17:09 2018/4/24
 * @Modified by:
 */
@Service
public class Receiver {

    private static final Logger log = LoggerFactory.getLogger(Receiver.class);

    @KafkaListener(topics = "${app.topic.test}")
    public void listen(@Payload String message) {
        log.info("received message='{}'", message);
    }
}
