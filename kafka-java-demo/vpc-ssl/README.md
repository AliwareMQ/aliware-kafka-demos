# Run Demo

1. 安装软件：确保安装了 JDK 8+ 和 Maven 3.2.5+
2. 编写配置：按照本页下面的接入说明配置`src/main/resource/kafka.properties`，然后修改kafka_client_jaas.conf中的用户名和密码
3. 发送消息：sh run_demo.sh producer
4. 消费消息：sh run_demo.sh consumer


# Java SDK接入说明

#### 1、Maven 依赖配置

```java
//消息队列 Kafka服务端版本是0.10系列，客户端建议使用该版本
<dependency>
       <groupId>org.apache.kafka</groupId>
       <artifactId>kafka-clients</artifactId>
       <version>2.4.0</version>
</dependency>
<dependency>
```
#### 2、SASL 配置  
 
消息队列 Kafka利用SASL机制对客户端进行身份验证。       
##### 2.1 创建文本文件 kafka\_client\_jaas.conf
可以使用Demo库中的文件进行修改，用户名密码可在kafka控制台上的实例详情获取，内容形式如下：

```
KafkaClient {
  org.apache.kafka.common.security.plain.PlainLoginModule required
  username="XXXXXXX"
  password="XXXXXXX";
};
```
  
注意：把\*\*\*替换为阿里云Kafka控制台实例详情页的用户名和密码。

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
见 JavaKafkaConfigurer
```

4.3 发送消息
```
见 KafkaProducerDemo
```

4.4 消费消息
```
见 KafkaConsumerDemo
```


