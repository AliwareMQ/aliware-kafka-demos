## 简介

用于迁移Topic与ConsumerGroup的metadata到阿里云[消息队列Kafka](kafka.console.aliyun.com)。

## 打包

```
sh bin/run.sh

```

## 运行


### 查看所有命令

```
java -jar kafka-migration.jar --cmdList
     TopicMigrationFromAliyun - Topic migration from aliyun
     TopicMigrationFromZk - Topic migration from zk
     ConsumerGroupMigrationFromTopic - ConsumerGroup migration from Aliyun
     ConsumerGroupMigrationFromAliyun - ConsumerGroup migration from Aliyun
```

### 具体命令

```
java -jar kafka-migration.jar --help TopicMigrationFromAliyun
Usage: TopicMigrationFromAliyun [options]
  Options:
  * --destAk
      ak of the dest instance
  * --destSk
      sk of the dest instance
  * --destRegionId
      region id of the dest instance
  * --destInstanceId
      dest instance id
    --commit
      if not specify, the command will only check whether can execute.
      Default: false
  * --sourceAk
      ak of the source instance
  * --sourceInstanceId
      source instance id
  * --sourceRegionId
      region id of the source instance
  * --sourceSk
      sk of the source instance
```

### 迁移Topic Metadata到阿里云消息队列Kafka

#### 简介

根据来源集群中的Topic Metadata，在阿里云消息队列Kafka的控制台中创建相应Topic。注意：迁移的内容仅为topic基本信息，不包括topic中的消息。
本Demo支持以下两种形式Topic Meta迁移：
1. 用户自建集群zookeeper——>消息队列Kafka；
2. 消息队列Kafka——>消息队列Kafka；

#### 用户自建集群zookeeper——>消息队列Kafka

```
java -jar kafka-migration.jar TopicMigrationFromZk --sourceZkConnect {sourceZkConnect} --destAk {destAk} --destSk {destSk} --destRegionId {destRegionId} --destInstanceId {destInstanceId} 

```

参数说明：
```
--sourceZkConnect 自建集群zk地址，将从该zk中读取topic metadata
--destAk 目标实例ak
--destSk 目标实例sk
--destRegionId 目标实例所在regionId，如cn-hangzhou, cn-beijing
--destInstanceId 目标实例id
--commit 可选参数。不加--commit时，仅在日志中打印将要迁移的topic。确认无误后，再次执行命令并增加参数--commit，topic metadata会实际迁移到消息队列kafka。
```

#### 消息队列Kafka——>消息队列Kafka

```
java -jar kafka-migration.jar TopicMigrationFromAliyun --sourceAk {sourceAk} --sourceSk {sourceSk} --sourceRegionId {sourceRegionId} --sourceInstanceId {sourceInstanceId} --destAk {destAk} --destSk {destSk} --destRegionId {destRegionId} --destInstanceId {destInstanceId} 

```

参数说明：
```
--sourceAk 源实例ak
--sourceSk 源实例sk
--sourceRegionId 源实例所在regionId，如cn-hangzhou, cn-beijing
--sourceInstanceId 源实例id
--destAk 目标实例ak
--destSk 目标实例sk
--destRegionId 目标实例所在regionId，如cn-hangzhou, cn-beijing
--destInstanceId 目标实例id
--commit 可选参数。不加--commit时，仅在日志中打印将要迁移的topic。确认无误后，再次执行命令并增加参数--commit，topic metadata会实际迁移到消息队列kafka。
```

#### 验证Topic Metadata迁移成功

建议迁移时，先不加--commit参数执行一次，确认要迁移的topic信息无误。
迁移完成后，可以在Kafka控制台查看来源kafka集群中的topic是否都已经成功创建。


### 迁移Group Metadata到阿里云消息队列Kafka

#### 简介

根据来源集群中的ConsumerGroup Metadata，在阿里云消息队列Kafka的控制台中创建相应ConsumerGroup。注意：迁移的内容仅为ConsumerGroup基本信息，具体消费的topic及位点信息不会迁移。
本Demo支持以下两种形式ConsumerGroup Meta迁移：
1. 用户自建集群Topic：__consumer_offsets中ConsumerGroup信息——>消息队列Kafka；
2. 消息队列Kafka——>消息队列Kafka；

#### 用户自建集群Topic：__consumer_offsets中ConsumerGroup信息——>消息队列Kafka

```
java -jar kafka-migration.jar ConsumerGroupMigrationFromTopic --propertiesPath {propertiesPath} --destAk {destAk} --destSk {destSk} --destRegionId {destRegionId} --destInstanceId {destInstanceId} 

```

参数说明：
```
--propertiesPath 自建集群配置文件，用于构造KafkaConsumer，从自建集群获取__consumer_offsets中消息
--destAk 目标实例ak
--destSk 目标实例sk
--destRegionId 目标实例所在regionId，如cn-hangzhou, cn-beijing
--destInstanceId 目标实例id
--commit 可选参数。不加--commit时，仅在日志中打印将要迁移的consumer group。确认无误后，再次执行命令并增加参数--commit，topic metadata会实际迁移到消息队列kafka。
```

配置文件kafka.properties说明：
```
## 接入点
bootstrap.servers=localhost:9092

## ConsumerGroup，用于消费__consumer_offsets中的消息。注意该consumer group不能有__consumer_offsets位点信息，保证能从第一个消息开始消费。
group.id=XXX

## 以下内容在无安全配置的情况下可以不配

## SASL鉴权方式
#sasl.mechanism=PLAIN

## 接入协议
#security.protocol=SASL_SSL

## ssl 根证书的路径，demo中有，请拷贝到自己的某个目录下，不能被打包到jar中
#ssl.truststore.location=/Users/yemaowei/Documents/code/aliware-kafka-demos/kafka-java-demo/vpc-ssl/src/main/resources/kafka.client.truststore.jks

## ssl 密码
#ssl.truststore.password=KafkaOnsClient

## sasl路径，demo中有，请拷贝到自己的某个目录下，不能被打包到jar中
#java.security.auth.login.config=/Users/yemaowei/Documents/code/aliware-kafka-demos/kafka-java-demo/vpc-ssl/src/main/resources/kafka_client_jaas.conf
```

#### 消息队列Kafka——>消息队列Kafka

```
java -jar kafka-migration.jar ConsumerGroupMigrationFromAliyun --sourceAk {sourceAk} --sourceSk {sourceSk} --sourceRegionId {sourceRegionId} --sourceInstanceId {sourceInstanceId} --destAk {destAk} --destSk {destSk} --destRegionId {destRegionId} --destInstanceId {destInstanceId} 

```

参数说明：
```
--sourceAk 源实例ak
--sourceSk 源实例sk
--sourceRegionId 源实例所在regionId，如cn-hangzhou, cn-beijing
--sourceInstanceId 源实例id
--destAk 目标实例ak
--destSk 目标实例sk
--destRegionId 目标实例所在regionId，如cn-hangzhou, cn-beijing
--destInstanceId 目标实例id
--commit 可选参数。不加--commit时，仅在日志中打印将要迁移的consumer group。确认无误后，再次执行命令并增加参数--commit，topic metadata会实际迁移到消息队列kafka。
```

#### 验证ConsumerGroup Metadata迁移成功

建议迁移时，先不加--commit参数执行一次，确认要迁移的ConsumerGroup信息无误。
迁移完成后，可以在Kafka控制台查看来源kafka集群中的ConsumerGroup是否都已经成功创建。



