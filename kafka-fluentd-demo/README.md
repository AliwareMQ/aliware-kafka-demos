# Run Demo

本Demo对Kafka与Fluentd集成的进行说明，主要可以作为Kafka_input(由fluentd消费Kafka消息)和Kafka_output(由fluend将消息发送到Kafka)

## 运行步骤

(注意：本Demo演示采用Max OS操作系统)

### 1. 下载和安装fluentd
Mac版本[td-agent-3.1.1-0.dmg](https://docs.fluentd.org/v1.0/articles/install-by-dmg) 参考此文档安装。并按照文档给出的命令启动td-agent，验证相关功能，然后停止td-agent.
```
a. 启动td-agent命令: sudo launchctl load /Library/LaunchDaemons/td-agent.plist
b. 按文档提示验证是否启动正常，以及发送debug消息是否可以在日志中体现
c. 停止td-agent命令: sudo launchctl unload /Library/LaunchDaemons/td-agent.plist
```

### 2. 替换fluentd的部分安装文件
进入fluentd安装目录结构, 其中两个子目录与Kafka plugin相关:
```
/opt/td-agent/embedded/lib/ruby/gems/2.4.0/gems/ruby-kafka-0.5.1/lib/kafka/ 
  | -- fluent-plugin-kafka-0.6.5/
     | -- lib/fluent/plugin/
        | -- in_kafka_group.rb [1]
        | -- out_kafka.rb      [2]
  | -- ruby-kafka-0.5.1/
     | -- lib/kafka/protocol/
        | -- fetch_request.rb  [3] 
```
在本demo目录下的对应目录结构下找到上述[1],[2],[3]文件，分别对安装目录下的三个文件进行修改或替换 （建议操作前对原始文件进行备份)，这三个文件修改的具体内容参见这个[git commit](https://github.com/AliwareMQ/aliware-kafka-demos/commit/ad63e4e5449e485f48e14bcce9596a1ad96f8312)

提示: 如果用户现场使用的Confluentd版本里使用的不是fluent-plugin-kafka-0.6.5和ruby-kafka-0.5.1，可以按照上面的commit检查相关的文件；如果有必要可以尝试使用demo目录下的相关文件进行替换整个目录，但并不能保证一定能工作。

### 3. 修改fluentd配置文件 /private/etc/td-agent/td-agent.conf
增加kafka-in和kafka-out两个配置，参考demo目录下的td-agent.plist文件 
```
## Kafka input
<source>
  @type kafka_group

  brokers XXX  # 接入地址从控制台实例信息中获得，如xxx1:9093,xxx2:9093,xxx3:9093
  
  topics  XXX  # Fluentd从Kafka的Topic消费, 如: FluentdInput
  format text
  message_key 
  add_prefix debug  # 消息的tag信息，这里配置debug tag可以将消费到的Kafka消息使用stdout输出到td-agent.log
  consumer_group XXX # Kafka上定义的消费组 

  # ruby-kafka consumer options
  #max_bytes     (integer) :default => nil (Use default of ruby-kafka)
  #max_wait_time (integer) :default => nil (Use default of ruby-kafka)
  #min_bytes     (integer) :default => nil (Use default of ruby-kafka)
  
  #注：以下三个参数只针对SASL_SSL方式访问需要配置，对于vpc内访问不需要如下三个参数
  ssl_ca_cert 'FULL-PATH-OF-alikafa-sasl.cert' # alikafa-sasl.cert文件的完整路径
  sasl_plain_username 'XXX'  # 从控制台获取实例的用户名
  sasl_plain_password 'XXX'  # 从控制台获取实例的密码
</source>

## Kafka output
<match kafka.**> # 将tag前缀为kafka的消息发送到指定的Kafka topic中
  @type kafka

  brokers    XXXX # 接入地址从控制台实例信息中获得，如xxx1:9093,xxx2:9093,xxx3:9093

  default_topic         XXX  # Fluentd将消息发送到Kafka的Topic, 如: FluentdOutput
  default_partition_key nil
  default_message_key   nil
  output_data_type      json
  output_include_tag    false
  output_include_time   false
  exclude_topic_key     false
  exclude_partition_key false

  # ruby-kafka producer options
  #max_send_retries    (integer)     :default => 1
  #required_acks       (integer)     :default => -1
  #ack_timeout         (integer)     :default => nil (Use default of ruby-kafka)
  #compression_codec   (gzip|snappy) :default => nil
  #max_buffer_size     (integer)     :default => nil (Use default of ruby-kafka)
  #max_buffer_bytesize (integer)     :default => nil (Use default of ruby-kafka)
  
  #注：以下三个参数只针对SASL_SSL方式访问需要配置，对于vpc内访问不需要如下三个参数
  ssl_ca_cert 'FULL-PATH-OF-alikafa-sasl.cert' # alikafa-sasl.cert文件的完整路径
  sasl_plain_username 'XXX'  # 从控制台获取实例的用户名
  sasl_plain_password 'XXX'  # 从控制台获取实例的密码
  
</match>  
```

#### 说明: 
1. alikafa-sasl.cert文件在本demo目录下可以找到，这个文件是使用如下步骤生成的：
  a. 从Aliyun Alikafka官方文档上[下载kafka.client.truststore.jks](http://common-read-files.oss-cn-shanghai.aliyuncs.com/kafka.client.truststore.jks?spm=a2c4g.11186623.2.16.cdf962b1rtKlQp&file=kafka.client.truststore.jks)
  b. 使用如下命令从truststore中导出证书
```
keytool -exportcert -keystore kafka.client.truststore.jks -storepass KafkaOnsClient -v -alias caroot -file alikafa-sasl.cert 
```
2. 配置文件中指定的topic和消费组信息要预先在控制台实例下创建好

### 4. 启动td-agent, 验证与Kafka的收发

a. 启动 sudo launchctl load /Library/LaunchDaemons/td-agent.plist
b. 测试KafkaInputPlugin：启动Kafka producer客户端， 向Kafka服务的topic（如: FluentdInput）发送消息， 同时监控conflentd日志(/private/var/log/td-agent/td-agent.log) 看是否有kafka消息被消费到。
c. 测试KafkaOutputPlugin： 启动Kafka producer消费端（注意不要使用KafkaInput里定义的消费组*）从Kafka服务的topic（如: FluentdOutput）消费消息，然后使用如下命令向Confluentd发送测试消息，并查看Kafka消费端是否消费到该消息。

```
curl -X POST -d 'json={"json":"message"}' http://localhost:8888/kafka.test
```