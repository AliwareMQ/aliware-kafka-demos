## KafkaOnsDemo
	
### 接入说明

请先阅读[上一层目录的README](https://github.com/AliwareMQ/aliware-kafka-demos)


### 接入步骤四：配置参数
对于Kafka Producer和Consumer都需要以下几个配置：

|相关文件|参数|值|
|:--|:--|:--|
|kafka_client_jaas.conf|AccessKey|阿里云账号的AccessKey|
|kafka_client_jaas.conf|SecretKey|阿里云账号的SecretKey|
|producer/consumer.xml|ssl.truststore.location|{XXX，自己的路径}/kafka.client.truststore.jks|
|producer/consumer.xml|group.id|请修改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的ConsumerID|
|producer/consumer.xml|defaultTopic|请修改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的Topic(类型为Kafka消息)|

除此之外，注意KafkaSaslConfig类的bean一定要配置在xml的最前方

### 报错“Failed to send SSL close message”
该错误后面通常还会跟“connection reset by peer”或“broken pipe”。该错误可以忽略，不影响使用。服务端是VIP网络环境，会主动掐掉空闲连接。
你可以通过修改日志级别来避免该错误，以log4j为例，加上下面这行配置：
`log4j.logger.org.apache.kafka.common.network.SslTransportLayer=ERROR`



	


