package com.alibaba.migration.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class JavaKafkaConfigurer {

    private static Properties properties;

    public static void configureSasl(String path) {
        //如果用-D或者其它方式设置过，这里不再设置
        if (null == System.getProperty("java.security.auth.login.config")) {
            //请注意将XXX修改为自己的路径
            //这个路径必须是一个文件系统可读的路径，不能被打包到jar中
            String configFilePath = getKafkaProperties(path).getProperty("java.security.auth.login.config");
            if(configFilePath != null){
                System.setProperty("java.security.auth.login.config", configFilePath);
            }
        }
    }

    public synchronized static Properties getKafkaProperties(String path) {
        if (null != properties) {
            return properties;
        }
        //获取配置文件kafka.properties的内容
        Properties kafkaProperties = new Properties();
        try {
            File file = new File(path);
            kafkaProperties.load(new FileInputStream(file));
        } catch (Exception e) {
            //没加载到文件，程序要考虑退出
            e.printStackTrace();
        }
        properties = kafkaProperties;
        return kafkaProperties;
    }


}
