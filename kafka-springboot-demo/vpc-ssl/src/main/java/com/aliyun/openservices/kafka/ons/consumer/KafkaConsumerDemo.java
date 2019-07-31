package com.aliyun.openservices.kafka.ons.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@SpringBootApplication()
@PropertySource(value = {"classpath:consumer.yml"})
public class KafkaConsumerDemo {

    public static void main(String[] args) {
        //如果不想利用这种方式注入，可以在java启动时，加上-Djava.security.auth.login.config=XXX来实现
        if (System.getProperty("java.security.auth.login.config") == null) {
            System.setProperty("java.security.auth.login.config", KafkaConsumerDemo.class.getClassLoader().getResource("kafka_client_jaas.conf").getPath());
        }
        SpringApplication.run(KafkaConsumerDemo.class, args);
    }

    @Component
    public static class KafkaReceiveService {

        @KafkaListener(topics = {"XXX"})
        public void listen(ConsumerRecord<?, ?> record) {

            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
            if (kafkaMessage.isPresent()) {
                Object message = kafkaMessage.get();
                System.out.println("----------------- record =" + record);
                System.out.println("------------------ message =" + message);
            }
            System.exit(0);
        }
    }
}