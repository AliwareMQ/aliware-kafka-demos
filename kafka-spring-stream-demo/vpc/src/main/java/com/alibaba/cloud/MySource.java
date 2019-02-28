package com.alibaba.cloud;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface MySource {

    String OUTPUT = "mySource";

    @Output(MySource.OUTPUT)
    MessageChannel output();
}
