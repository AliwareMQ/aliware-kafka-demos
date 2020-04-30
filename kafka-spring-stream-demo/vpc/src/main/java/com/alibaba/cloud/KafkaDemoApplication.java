package com.alibaba.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootApplication
@EnableBinding({MyInput.class, MyOutput.class})
public class KafkaDemoApplication {


    public static void main(String[] args) {
        SpringApplication.run(KafkaDemoApplication.class, args);
    }

    @StreamListener(MyInput.INPUT)
    public void log(String payload) {
        System.out.println("Receive:" + payload);
    }

    @Bean
    @InboundChannelAdapter(value = MyOutput.OUTPUT, poller = @Poller(fixedDelay = "1000", maxMessagesPerPoll = "1"))
    public MessageSource<String> timerMessageSource() {
        return () -> {
            String value = " hello word !! ";
            System.out.println("Send:" + value);
            return MessageBuilder.withPayload(value).build();
        };
    }
}
