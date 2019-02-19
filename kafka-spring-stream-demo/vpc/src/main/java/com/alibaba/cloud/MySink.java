package com.alibaba.cloud;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface MySink {
    String INPUT = "mySink";

    @Input(MySink.INPUT)
    SubscribableChannel input();
}
