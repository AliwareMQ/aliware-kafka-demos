package com.aliyun.openservices.kafka.config;

import org.springframework.stereotype.Component;

/**
 * @Author ：yaxuSong
 * @Description:
 * @Date: 17:27 2018/4/24
 * @Modified by:
 */
@Component
public class KafkaSaslConfig {
    public KafkaSaslConfig() {
        //生产环境部署时，请修改为真正的路径
        if (System.getProperty("java.security.auth.login.config") == null) {
            System.setProperty("java.security.auth.login.config", KafkaSaslConfig.class.getClassLoader().getResource("kafka_client_jaas.conf").getPath());
        }
    }
}
