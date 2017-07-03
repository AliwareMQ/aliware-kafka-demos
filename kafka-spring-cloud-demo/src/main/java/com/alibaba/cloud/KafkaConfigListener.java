package com.alibaba.cloud;

import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

public class KafkaConfigListener implements ApplicationListener<ApplicationStartingEvent> {
    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        //生产环境部署时，请修改为真正的路径
        if (System.getProperty("java.security.auth.login.config") == null) {
            System.setProperty("java.security.auth.login.config", KafkaConfigListener.class.getClassLoader().getResource("kafka_client_jaas.conf").getPath());
        }
    }
}
