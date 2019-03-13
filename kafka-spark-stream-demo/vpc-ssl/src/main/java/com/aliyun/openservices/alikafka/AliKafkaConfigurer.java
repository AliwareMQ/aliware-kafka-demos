package com.aliyun.openservices.alikafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AliKafkaConfigurer {
    private static Properties properties;

    //设置SASL_SSL协议所对应的系统属性和kafka属性，后者将设置到相应的producer或consumer
    public synchronized static Map<String, String> saslSSLConfig() {
        if (System.getProperty("java.security.auth.login.config") == null) {
            System.setProperty("java.security.auth.login.config", AliKafkaConfigurer.class.getClassLoader().getResource("kafka_client_jaas.conf").getPath());
        }

        Map<String, String> retMap = new HashMap<String, String>();

        //设置SSL根证书的路径，请记得将XXX修改为自己的路径
        //与sasl路径类似，该文件也不能被打包到jar中
        retMap.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, AliKafkaConfigurer.class.getClassLoader().getResource("kafka.client.truststore.jks").getPath());
        //根证书store的密码，保持不变
        retMap.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "KafkaOnsClient");
        //接入协议，目前支持使用SASL_SSL协议接入
        retMap.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        //SASL鉴权方式，保持不变
        retMap.put(SaslConfigs.SASL_MECHANISM, "PLAIN");

        return retMap;
    }

    //获取kafka.properties属性文件
    public synchronized static Properties getKafkaProperties() {
        if (null != properties) {
            return properties;
        }
        //获取配置文件kafka.properties的内容
        Properties kafkaProperties = new Properties();
        try {
            kafkaProperties.load(AliKafkaConfigurer.class.getClassLoader().getResourceAsStream("kafka.properties"));
        } catch (Exception e) {
            //没加载到文件，程序要考虑退出
            e.printStackTrace();
        }
        properties = kafkaProperties;
        return kafkaProperties;
    }
}
