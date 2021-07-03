package com.aliyun.openservices.kafka.ons.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@SpringBootApplication(scanBasePackages = {"com.aliyun.openservices.kafka.ons.producer"})
@PropertySource(value = {"classpath:producer.yml"})
public class KafkaProducerDemo {

    public static void main(String[] args) throws Exception {
        //如果不想利用这种方式注入（jar方式运行时必须用-D配置），可以在java启动时，加上-Djava.security.auth.login.config=XXX来实现
        if (System.getProperty("java.security.auth.login.config") == null) {
            System.setProperty("java.security.auth.login.config", KafkaProducerDemo.class.getClassLoader().getResource("kafka_client_jaas.conf").getPath());
        }
        ConfigurableApplicationContext context = SpringApplication.run(KafkaProducerDemo.class, args);
        context.getBean(MessageSendService.class).sendOneMessage();

    }

    @Component
    public static class MessageSendService {

        @Autowired
        private KafkaTemplate<String, String> kafkaTemplate;

        public void sendOneMessage() throws Exception {
            ListenableFuture<SendResult<String, String>> result = kafkaTemplate.send("XXX", "hello aliKafka");
            // 因为linger.ms配置的是1ms，所以可以不用着急flush
            //kafkaTemplate.flush();
            System.out.println("producer send ok " + result.get().getProducerRecord());
        }
    }
}