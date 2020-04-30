package com.alibaba.cloud;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface MyInput {
    String INPUT = "MyInput";

    @Input(MyInput.INPUT)
    SubscribableChannel input();
}
