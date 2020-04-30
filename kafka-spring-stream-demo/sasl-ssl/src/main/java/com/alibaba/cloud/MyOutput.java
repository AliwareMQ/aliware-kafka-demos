package com.alibaba.cloud;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface MyOutput {

    String OUTPUT = "MyOutput";

    @Output(MyOutput.OUTPUT)
    MessageChannel output();
}
