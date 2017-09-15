package com.aliyun.openservices.kafka.ons;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;

public class ConsumerMessageListener implements MessageListener<String,String>{
    @Override
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        System.out.println(String.format("Key:%s value:%s", consumerRecord.key(), consumerRecord.value()));

    }
}
