# Run Demo

1. 安装软件：确保安装了 JDK 8+ 和 Maven 3.2.5+
2. 编写配置：按照本页下面的接入说明配置`src/main/resource/kafka.properties`
3. 发送消息：sh run_producer.sh
4. 消费消息：sh run_consumer.sh


# Java SDK接入说明


#### 1、Maven 依赖配置

```java
//消息队列 Kafka服务端版本是0.10.0.0，客户端建议使用该版本
<dependency>
       <groupId>org.apache.kafka</groupId>
       <artifactId>kafka-clients</artifactId>
       <version>0.10.0.0</version>
</dependency>
<dependency>
//SASL鉴权使用的库
<groupId>com.aliyun.openservices</groupId>
      <artifactId>ons-sasl-client</artifactId>
      <version>0.1</version>
</dependency>
```

#### 2、SASL 配置  
 
消息队列 Kafka利用SASL机制对客户端进行身份验证。       
##### 2.1 创建文本文件 kafka\_client\_jaas.conf

可以使用Demo库中的文件进行修改，内容形式如下：

```
KafkaClient {

   com.aliyun.openservices.ons.sasl.client.OnsLoginModule required

   AccessKey="***"

   SecretKey="***";

};

```
  
注意：把\*\*\*替换为阿里云账号的 AccessKey，SecretKey。

##### 2.2 设置 kafka\_client\_jaas.conf 的路径：

kafka\_client\_jaas.conf的路径是**系统变量**，有两种办法进行设置（这里假设 kafka\_client\_jaas.conf 放在/home/admin 下面，实际部署时请注意修改为自己的路径）：
   
   程序启动时，启动 JVM 参数：

 ```bash
 -Djava.security.auth.login.config=/home/admin/kafka_client_jaas.conf
 ```

 或者在代码中设置参数（需要保证在 Kafka Producer 和 Consumer 启动之前）:

 ```bash
 System.setProperty("java.security.auth.login.config", “/home/admin/kafka_client_jaas.conf");
 ```

#### 3. SSL配置

下载根证书

[下载地址](http://common-read-files.oss-cn-shanghai.aliyuncs.com/kafka.client.truststore.jks)

下载后放入某个目录下，其路径需要直接配置在代码中。


#### 4.示例代码

4.1 准备配置文件kafka.properties，可以参考Demo中的进行修改

```
## 接入点，通过控制台获取
## 您在控制台获取的SSL接入点
bootstrap.servers=xxx:9093,xxx:9093,xxx:9093

## Topic，通过控制台创建
## 您在控制台创建的Topic
topic=alikafka-topic-demo

## Consumer Grouo，通过控制台创建
## 您在控制台创建的 Consumer Group
group.id=CID-consumer-group-demo

## ssl 根证书的路径，demo中有，请拷贝到自己的某个目录下，不能被打包到jar中
## 这里假设您的目录为/home/admin，请记得修改为自己的实际目录
ssl.truststore.location=/home/admin/kafka.client.truststore.jks

## sasl路径，demo中有，请拷贝到自己的某个目录下，不能被打包到jar中
## 这里假设您的目录为/home/admin，请记得修改为自己的实际目录
## 控制台实例详情获取用户名密码
java.security.auth.login.config=/home/admin/kafka_client_jaas.conf
```
4.2 加载配置文件
```
private static Properties properties;

public static void configureSasl() {
   //如果用-D或者其它方式设置过，这里不再设置
   if (null == System.getProperty("java.security.auth.login.config")) {
       //请注意将XXX修改为自己的路径
       //这个路径必须是一个文件系统可读的路径，不能被打包到jar中
       System.setProperty("java.security.auth.login.config", getKafkaProperties().getProperty("java.security.auth.login.config"));
   }
}

public synchronized static Properties getKafkaProperties() {
   if (null != properties) {
       return properties;
   }
   //获取配置文件kafka.properties的内容
   Properties kafkaProperties = new Properties();
   try {
       kafkaProperties.load(KafkaProducerDemo.class.getClassLoader().getResourceAsStream("kafka.properties"));
   } catch (Exception e) {
       //没加载到文件，程序要考虑退出
       e.printStackTrace();
   }
   properties = kafkaProperties;
   return kafkaProperties;
}
```

4.3 发送消息
```
见 KafkaProducerDemo
```

4.4 消费消息
```
见 KafkaConsumerDemo
```


