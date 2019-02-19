package com.alibaba.cloud;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * message output test
 *
 * @author linux_china
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MessageOutputTest {
    @Autowired
    private Source source;

    //用户自定义的source: myChannel
    //操作流程：
    //1.定义interface MySource
    //2.在KafkaDemoApplication中@EnableBinding中加上MySource.class
    //3.在application.properties中为myChannel添加destination的配置
    //3.在使用的地方@Autowired即可
    //4.自定义sink也是类似操作,具体参考官网
    // http://docs.spring.io/spring-cloud-stream/docs/Chelsea.SR2/reference/htmlsingle/index.html#_customizing_channel_names
    @Autowired
    private MySource mySource;

    @Test
    public void testSend() {
        Message<String> message = MessageBuilder.withPayload("Hello Aliyun Kafka")
                .build();
        source.output().send(message);
        mySource.output().send(message);
    }
}
