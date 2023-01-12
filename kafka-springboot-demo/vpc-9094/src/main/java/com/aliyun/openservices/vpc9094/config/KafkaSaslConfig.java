package com.aliyun.openservices.vpc9094.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author liujiang
 */
public class KafkaSaslConfig {


    private static final String SCRAM_LOGIN_MODULE = "org.apache.kafka.common.security.scram.ScramLoginModule";
    private static final String PLAIN_LOGIN_MODULE = "org.apache.kafka.common.security.plain.PlainLoginModule";
    private static final String PLAIN = "PLAIN";

    private KafkaSaslConfig() {
    }

    public static void kafkaSaslConfig(Map<String, Object> props, PropertiesConfig propertiesConfig) {
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, propertiesConfig.getSecurityProtocolConfig());
        // 设置SASL账号
        String saslMechanism = propertiesConfig.getSaslMechanism();
        String username = propertiesConfig.getUsername();
        String password = propertiesConfig.getPassword();
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            String prefix = SCRAM_LOGIN_MODULE;
            if (PLAIN.equalsIgnoreCase(saslMechanism)) {
                prefix = PLAIN_LOGIN_MODULE;
            }
            String jassConfig = String.format("%s required username='%s' password='%s';", prefix, username, password);
            props.put(SaslConfigs.SASL_JAAS_CONFIG, jassConfig);
        }
        props.put(SaslConfigs.SASL_MECHANISM, saslMechanism);

    }
}
