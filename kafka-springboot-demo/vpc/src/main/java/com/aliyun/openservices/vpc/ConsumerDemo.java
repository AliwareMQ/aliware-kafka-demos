package com.aliyun.openservices.vpc;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author liujiang
 */
@Slf4j
@Component
public class ConsumerDemo {

    @KafkaListener(topics = "${spring.kafka.producer.topic}")
    public void listenerMessage(ConsumerRecord<?, ?> record) {
        String format = String.format("topic:{ %s }, partitionId:{ %s }, offset:{ %s } value:{ %s }",
                record.topic(), record.partition(), record.offset(), record.value());
        log.info(format);

    }
}
