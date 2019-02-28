### 前言
以下为使用spring-cloud-stream使用阿里云Kafka示例，其中示例分类为VPC内示例(内网)和公网SSL示例，可根据购买的实例信息选择相应实例运行。

### 安装必备
1. Maven 3.2.5+
2. JDK 8+ 

### 示例简介
以下以binder平台为阿里云kafka对示例进行简单的描述。样例工程中通过poller发送消息到，然后通过listener监听收取消息。

![image](../images/spring-cloud-kafka-demo.png)
 
1. 示例中mySource即测试的输入源定义，同时需要在配置文件中定义destination和数据格式
2. 示例中mySink即测试的输出源定义，同时需要在配置文件中定义group、destination和数据格式

### 示例配置

#### VPC内示例配置

将application.properties中的"XXX"替换为实际值

```
kafka.bootstrap-servers=XXX
kafka.consumer.group=XXX
kafka.topic.name=XXX
```
#### SSL示例配置

1. 修改application.properties中的"XXX"替换为实际值

```
kafka.bootstrap-servers=XXX
kafka.consumer.group=XXX
kafka.topic.name=XXX
kafka.ssl.truststore.location=/XXX/kafka.client.truststore.jks
```

2. 修改kafka_client_jaas.conf中的username和password，可从实例详情获取。

```
KafkaClient {
  org.apache.kafka.common.security.plain.PlainLoginModule required
  username="XXX"
  password="XXX";
};

```

### 示例运行
sh run_demo.sh 