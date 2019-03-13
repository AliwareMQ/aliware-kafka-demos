# Run Demo

本Demo演示了Apache Flume在source上收集日志文件变化，并在Sink端将日志文件的内容作为消息发布到Kafka Topic上的配置过程，整个过程不需要代码的编写，Sink端直接使用了Flume提供的org.apache.flume.sink.kafka.KafkaSink.

## 运行步骤

### 1. 安装Flume, 如[1.7](http://archive.apache.org/dist/flume/1.7.0/)
### 2. 在AliKafka控制台上创建或确定要测试的实例，获取实例的如下信息:
* SSL接入点地址 (或 默认接入点地址，如果是测试在vpc内访问的情况)
* 例配置信息下获取用户名和密码 
* 创建一个用于接收Flume消息的Topic, 如: FlumeKafkaTest

### 3. 设置Flume Agent配置文件, 如 kafka-recv.properties
* 设置文件中的属性 agent.sinks.k1.kafka.bootstrap.servers 值为第2步骤中的接入地址
* 下载SSL TrustStore[kafka.client.truststore.jks](http://common-read-files.oss-cn-shanghai.aliyuncs.com/kafka.client.truststore.jks?spm=a2c4g.11186623.2.16.2d7762b13TwesV&file=kafka.client.truststore.jks), 并将本文件的完整路径设置给属性 agent.sinks.k1.kafka.producer.ssl.truststore.location  
```
# The configuration file needs to define the sources, 
# the channels and the sinks.
# Sources, channels and sinks are defined per agent, 
# in this case called 'agent'

agent.sources = r1
agent.channels = c1
agent.sinks = k1

#/** For sources **/
agent.sources.r1.type = exec
# The channel can be defined as follows.
agent.sources.r1.command=tail -F /tmp/kafka-flume.log

#/** For channel **/

#/* memory channel */
agent.channels.c1.type=memory
agent.channels.c1.capacity=10000
agent.channels.c1.transactionCapacity=100

#/** For sink **/
agent.sinks.k1.type= org.apache.flume.sink.kafka.KafkaSink
agent.sinks.k1.kafka.topic=FlumeKafkaTest
agent.sinks.k1.BatchSize = 5000
agent.sinks.k1.batchDurationMillis = 2000
agent.sinks.k1.kafka.bootstrap.servers = XXXXXXXX
#/** For SASL_SSL config begin **/
agent.sinks.k1.kafka.producer.security.protocol = SASL_SSL
agent.sinks.k1.kafka.producer.ssl.truststore.location = XXXXX/kafka.client.truststore.jks
agent.sinks.k1.kafka.producer.ssl.truststore.password = KafkaOnsClient
agent.sinks.k1.kafka.producer.security.protocol = SASL_SSL
agent.sinks.k1.kafka.producer.sasl.mechanism = PLAIN
#/** For SASL_SSL config end **/

#https://www.cloudera.com/documentation/enterprise/5-9-x/topics/cm_mc_flume_kafka_security_confg.html

# Bind the source and sink to the channel
agent.sources.r1.channels = c1
agent.sinks.k1.channel = c1
```

### 4. 设置Kafka SASL客户端配置文件, 如 kafka_client_jaas.conf 
* 并将在第2步中获取的Kafka实例的用户名和密码替换该文件的对应属性值
```
KafkaClient {
   org.apache.kafka.common.security.plain.PlainLoginModule required
   username="XXXXX"
   password="XXXXXXX";
};
```

### 5. 进入Flume安装目录，使用如下命令启动Flume, 注意替换kafka-recv.properties和kafka_client_jaas.conf文件的完整路径
```
sh bin/flume-ng agent --conf conf --conf-file FULL-PATH-OF-kafka_recv.properties --name agent  -Dflume.root.logger=INFO,console -Djava.security.auth.login.config=FULL-PATH-OF-kafka_client_jaas.conf
```

### 6. 测试

* 向文件/tmp/kafka-flume.log中不断地写入内容
* 可以看到kafka topic FlumeKafkaTest里不断的有内容加入 (可以参考../kafka-java-demo/ 消费并查看这个topic里的内容)


### 7. 其他说明

* 本示例是Flume使用公网方式(SASL_SSL)向Kafka发送消息，如果使用VPC内访问，则不需要设置sasl/ssl等相关的属性，也不需要在启动Flume时设置-Djava.security.auth.login.config