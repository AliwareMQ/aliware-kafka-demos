package com.aliyun.openservices.vpc9094.controller;

import com.aliyun.openservices.vpc9094.ProducerDemo;
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
    private ProducerDemo kafkaProducerDemo;

    @GetMapping("/send")
    public String sendMessage(){
        return kafkaProducerDemo.sendMessage();
    }
}