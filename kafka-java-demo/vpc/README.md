# Run Demo

1. 安装软件：确保安装了 JDK 8+ 和 Maven 3.2.5+
2. 编写配置：按照本页下面的接入说明配置`src/main/resource/kafka.properties`
3. 打包 sh build.sh
4. 发送消息 java -cp .:kafka-vpc-demo.jar com.aliyun.openservices.kafka.ons.KafkaProducerDemo
5. 消费消息 java -cp .:kafka-vpc-demo.jar com.aliyun.openservices.kafka.ons.KafkaConsumerDemo


# Java SDK接入说明

#### 1、Maven 依赖配置

```java
//消息队列 Kafka服务端版本是0.10系列，客户端建议使用该版本
<dependency>
       <groupId>org.apache.kafka</groupId>
       <artifactId>kafka-clients</artifactId>
       <version>2.4.0</version>
</dependency>
```

#### 2.示例代码

2.1 准备配置文件kafka.properties，可以参考Demo中的进行修改

```
## 接入点，通过控制台获取
## 您在控制台获取的接入点
bootstrap.servers=控制台上的接入点IP+Port

## Topic，通过控制台创建
## 您在控制台创建的Topic
topic=alikafka-topic-demo

## Consumer Grouo，通过控制台创建
## 您在控制台创建的 Consumer Group
group.id=CID-consumer-group-demo

2.2 加载配置文件
```
见 JavaKafkaConfigurer
```
2.3 发送消息
```
见 KafkaProducerDemo
```
2.4 消费消息
```
见 KafkaConsumerDemo
```

