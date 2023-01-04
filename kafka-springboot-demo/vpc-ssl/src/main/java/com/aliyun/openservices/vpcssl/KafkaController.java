package com.aliyun.openservices.vpcssl;

import com.aliyun.openservices.vpcssl.producer.KafkaProducerDemo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liujiang
 */
@RestController
@RequestMapping("/kafka")
public class KafkaController {

    @Autowired
    private KafkaProducerDemo kafkaProducerDemo;

    @GetMapping("/send")
    public String sendMessage(){
        return kafkaProducerDemo.sendMessage();
    }
}
