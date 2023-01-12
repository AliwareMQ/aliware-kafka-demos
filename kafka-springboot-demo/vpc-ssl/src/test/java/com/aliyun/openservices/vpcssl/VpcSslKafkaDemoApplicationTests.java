package com.aliyun.openservices.vpcssl;

import com.aliyun.openservices.vpcssl.producer.KafkaProducerDemo;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class VpcSslKafkaDemoApplicationTests {

    @Autowired
    private KafkaProducerDemo kafkaProducerDemo;

    @Test
    public void sendMessageTest() {
        for (int i = 0; i < 100; i++) {
            kafkaProducerDemo.sendMessage();
        }
    }

}
