## Demo简介

本demo将演示如何使用[Kafka MirrorMaker](https://cwiki.apache.org/confluence/pages/viewpage.action?pageId=27846330)，将用户自建集群中的topic数据迁移到阿里云[消息队列 KAFKA](https://www.aliyun.com/product/kafka)中。

## 迁移前准备工作

### 获取Kafka MirrorMaker
下载[Kafka](http://kafka.apache.org/downloads)。
以Kafka 2.2.0版本为例，下载后解压。

```shell
> tar -xzf kafka_2.12-2.2.0.tgz
> cd kafka_2.12-2.2.0
```

### 在阿里云[消息队列 KAFKA](https://www.aliyun.com/product/kafka)中创建需要迁移Topic

1. 提前在阿里云[消息队列 KAFKA](https://www.aliyun.com/product/kafka)的控制台中创建需要迁移的Topic，注意事项：
* Topic名称必须一致；
* Partition数量可以不一致；
* 迁移后原先在同一个partition中的数据并不保证迁移到同一个partition中。如果创建的topic为配置为分区顺序消息，则能保证相同key的消息分布在同一个partition中。

## 开始迁移Topic数据

执行以下命令，开启迁移进程，参数详解请参见[这里](https://cwiki.apache.org/confluence/pages/viewpage.action?pageId=27846330)
```shell
sh bin/kafka-mirror-maker.sh --consumer.config config/consumer.properties --producer.config config/producer.properties --whitelist topicName
```

### consumer.properties配置说明，详情参见[这里](https://kafka.apache.org/documentation/#consumerconfigs)
```
## 自建集群接入点
bootstrap.servers=XXX.XXX.XXX.XXX:9092
## 消费者分区分配策略
partition.assignment.strategy=org.apache.kafka.clients.consumer.RoundRobinAssignor
#consumer group id
group.id=test-consumer-group
```

### producer.properties配置说明，详情参见[这里](https://kafka.apache.org/documentation/#producerconfigs)

#### VPC接入
```
## 集群接入点
## Kafka接入点，通过控制台获取
## 您在控制台获取的默认接入点
bootstrap.servers=控制台上的接入点IP+Port

## 数据压缩方式
compression.type=none
```

#### 公网接入
producer.properties文件
```
## 集群接入点
## Kafka接入点，通过控制台获取
## 您在控制台获取的默认接入点
bootstrap.servers=控制台上的接入点IP+Port

## 数据压缩方式
compression.type=none

## truststore，使用本demo给的文件
ssl.truststore.location=kafka.client.truststore.jks
ssl.truststore.password=KafkaOnsClient
security.protocol=SASL_SSL
sasl.mechanism=PLAIN

## kafka 2.X版本在配置sasl接入时需要做以下配置。2.X以下版本不需要配置
ssl.endpoint.identification.algorithm=
```

公网接入，在启动kafka-mirror-maker.sh前，需要设置java.security.auth.login.config
```shell
> export KAFKA_OPTS="-Djava.security.auth.login.config=kafka_client_jaas.conf"
```

kakfa_client_jaas.conf文件
```
KafkaClient {
  org.apache.kafka.common.security.plain.PlainLoginModule required
  ## 控制台上提供的用户名和密码
  username="XXXXXXX"
  password="XXXXXXX";
};
```

### 关于配置文件的说明
以上示例consumer.properties与producer.properties，主要针对用户自建集群往阿里云Kafka迁移。
实际上，MirrorMaker支持各种集群之间迁移数据，但需要注意的是：目前MirrorMaker只支持一份密码。也就是说，如果迁移的源集群与目标集群都需要密码，且密码不一致，则不支持迁移。其余情况均支持迁移。

## 验证Kafka MirrorMaker成功运行

方式1：

通过kafka-consumer-groups.sh查看自建集群消费进度
```shell
bin/kafka-consumer-groups.sh --new-consumer --describe --bootstrap-server 自建集群接入点 --group test-consumer-group
```

方式2：
往自建集群中发送消息，在控制台中看topic分区状态，确认当前服务器上消息总量是否正确。
使用控制台消息查询功能，可以查看具体消息内容。
