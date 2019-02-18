package com.aliyun.openservices.kafka.ons;


public class KafkaSaslConfig  {

    //如果不想利用这种方式注入，可以在java启动时，加上-Djava.security.auth.login.config=XXX来实现
    public KafkaSaslConfig() {
        //生产环境部署时，请修改为真正的路径
        if (System.getProperty("java.security.auth.login.config") == null) {
            System.setProperty("java.security.auth.login.config", KafkaSaslConfig.class.getClassLoader().getResource("kafka_client_jaas.conf").getPath());
        }
    }
}
