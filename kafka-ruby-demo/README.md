# Run Demo

本Demo是演示使用Ruby作为Kafka的消费端和发送端的例子，主体代码基于[ruby-kafka-0.5.1](ruby-kafka-0.5.1/README.md), 并对代码进行局部微调。

## 运行步骤

(说明：本Demo在Max OS操作系统进行， Ruby命名版本： ruby 2.3.7p456)

### 配置消费端和发送端脚本

根据控制台上的AliKafka实例的接入地址，SASL用户名和密码，及Topic, 消费组信息。

1. 消费端脚本设置 ruby-kafka-0.5.1/examples/simple-consumer.rb，将代码中"XXXX"部分替换成具体的变量值： 

```
# Consumes lines from a Kafka partition and writes them to STDOUT.
#
$LOAD_PATH.unshift(File.expand_path("../../lib", __FILE__))

require "kafka"

# We don't want log output to clutter the console. Replace `StringIO.new`
# with e.g. `$stderr` if you want to see what's happening under the hood.
logger = Logger.new(StringIO.new)

brokers = "XXXX" # 控制台上获取的kafka实例的接入地址

# 注意： 以下三个配置项只有在SSL接入点时才需要配置，对于VPC内直连访问不需要设置
ssl_ca_cert_file = '../../alikafa-sasl.cert' # SASL证书文件，在本demo目录下
sasl_plain_username = "XXXX" # 控制台上kafka实例配置信息中用户名
sasl_plain_password = "XXXX" # 控制台上kafka实例配置信息中密码

# Make sure to create this topic in your Kafka cluster 
topic = "XXXX"  # 在控制台kafka实例上提前创建的测试Topic
groupId = "XXXX"  # 在控制台kafka实例上提前创建的测试消费组

puts "bootstrap.servers= #{brokers}"
puts "consume topic= `#{topic}`"
puts "consume group= `#{groupId}`"

kafka = Kafka.new(
  seed_brokers: brokers,
  client_id: "simple-consumer",
  socket_timeout: 20,
  logger: logger,
  # 注意：以下三个参数只有在SSL接入点时才需要设置
  ssl_ca_cert:  File.read(ssl_ca_cert_file),
  sasl_plain_username: sasl_plain_username,
  sasl_plain_password: sasl_plain_password,
)

consumer = kafka.consumer(group_id: groupId)
consumer.subscribe(topic)

trap("TERM") { consumer.stop }

#consumer.each_message(topic: topic) do |message|
#  puts message.value
#end

puts "-- Consume Result --"
consumer.each_message do |message|
  puts "topic-partition: #{message.topic}/#{message.partition} offset=#{message.offset}, key=#{message.key}, value=#{message.value}"
end

```

2. 发送端脚本设置 ruby-kafka-0.5.1/examples/simple-producer.rb， 将代码中"XXXX"部分替换成具体的变量值： 

```
# Reads lines from STDIN, writing them to Kafka.
#
$LOAD_PATH.unshift(File.expand_path("../../lib", __FILE__))

require "kafka"

logger = Logger.new($stderr)

brokers = "XXXX" # 控制台上获取的kafka实例的接入地址

# 注意： 以下三个配置项只有在SSL接入点时才需要配置，对于VPC内直连访问不需要设置
ssl_ca_cert_file = '../../alikafa-sasl.cert' # SASL证书文件，在本demo目录下
sasl_plain_username = "XXXX" # 控制台上kafka实例配置信息中用户名
sasl_plain_password = "XXXX" # 控制台上kafka实例配置信息中密码

# Make sure to create this topic in your Kafka cluster
topic = "XXXX"  # 在控制台kafka实例上提前创建的测试Topic

puts "bootstrap.servers= #{brokers}"
puts "producer topic= `#{topic}`"

kafka = Kafka.new(
  seed_brokers: brokers,
  client_id: "simple-producer",
  logger: logger,
  # 注意：以下三个参数只有在SSL接入点时才需要设置
  ssl_ca_cert:  File.read(ssl_ca_cert_file), 
  sasl_plain_username: sasl_plain_username,
  sasl_plain_password: sasl_plain_password,
)

producer = kafka.producer

begin
  $stdin.each_with_index do |line, index|
    producer.produce(line, topic: topic)

    # Send messages for every 3 lines.
    # 每输入三行作为一次批处理发送
    producer.deliver_messages if index % 3 == 0
  end
ensure
  # Make sure to send any remaining messages.
  producer.deliver_messages

  producer.shutdown
end
```

#### 说明: 
1. alikafa-sasl.cert文件在本demo目录下可以找到，这个文件是使用如下步骤生成的：
  a. 从Aliyun Alikafka官方文档上[下载kafka.client.truststore.jks](http://common-read-files.oss-cn-shanghai.aliyuncs.com/kafka.client.truststore.jks?spm=a2c4g.11186623.2.16.cdf962b1rtKlQp&file=kafka.client.truststore.jks)
  b. 使用如下命令从truststore中导出证书
```
keytool -exportcert -keystore kafka.client.truststore.jks -storepass KafkaOnsClient -v -alias caroot -file alikafa-sasl.cert 
```

###  测试
1. 启动消费端
```
a. 打开终端界面
b. cd ruby-kafka-0.5.1/examples/
c. ruby simple-consumer.rb   (启动消费端，等待发送端发送消息过来)
```

2. 启动消费端，并不断地发送消息

```
a. 打开另一个终端界面
b. cd ruby-kafka-0.5.1/examples/
c. ruby simple-producer.rb   (启动发送端，按行输入消息，每输入三行进行一次batch发送)
```

3. 查看结果，验证消息是否能正确发送和消费