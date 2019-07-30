package com.aliyun.openservices.kafka.ons;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Optional;

@SpringBootApplication
public class KafkaSpringbootDemo {

    public static void main(String[] args) throws Exception {
        System.setProperty("java.security.auth.login.config", KafkaSpringbootDemo.class.getClassLoader().getResource("kafka_client_jaas.conf").getPath());
        ConfigurableApplicationContext context = SpringApplication.run(KafkaSpringbootDemo.class, args);
        //给Consumer初始化一些时间，以便从latest开始消费
        Thread.sleep(6000);

        context.getBean(MessageSendService.class).sendOneMessage();

        Thread.sleep(3000);
        System.exit(0);
    }

    @Component
    public static class KafkaReceiveService {

        @KafkaListener(topics = {"kongming_test"})
        public void listen(ConsumerRecord<?, ?> record) {

            Optional<?> kafkaMessage = Optional.ofNullable(record.value());

            if (kafkaMessage.isPresent()) {

                Object message = kafkaMessage.get();

                System.out.println("----------------- record =" + record);
                System.out.println("------------------ message =" + message);
            }

        }
    }

    @Component
    public static class MessageSendService {

        @Autowired
        private KafkaTemplate<String, String> kafkaTemplate;

        public void sendOneMessage() throws Exception {
            ListenableFuture<SendResult<String, String>> result = kafkaTemplate.send("kongming_test", "hello aliKafka");
            kafkaTemplate.flush();
            System.out.println("producer send ok " + result.get().getProducerRecord());
        }
    }
}