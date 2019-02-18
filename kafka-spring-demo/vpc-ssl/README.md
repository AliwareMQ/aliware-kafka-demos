## Run Demo
1. 安装软件：确保安装了 JDK 8+ 和 Maven 3.2.5+
2. 编写配置：修改kafka-consumer.xml、kafka-producer.xml和kafka_client_jaas.conf中的 XXX 替换为实际值
3. 发送消息：sh run_demo.sh producer
4. 消费消息：sh run_demo.sh consumer

## 配置参数
对于Kafka Producer和Consumer都需要以下几个配置：

|相关文件|参数|值|
|:--|:--|:--|
|kafka_client_jaas.conf|username|控制台实例详情中的用户名|
|kafka_client_jaas.conf|password|控制台实例详情中的密码|
|producer/consumer.xml|ssl.truststore.location|{XXX，自己的路径}/kafka.client.truststore.jks|
|producer/consumer.xml|group.id|请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6)|
|producer/consumer.xml|defaultTopic|请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6)|
|producer/consumer.xml|bootstrap.servers|请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga)|

除此之外，注意KafkaSaslConfig类的bean一定要配置在xml的最前方
`log4j.logger.org.apache.kafka.common.network.SslTransportLayer=ERROR`



	


