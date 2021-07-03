## SpringBoot demo for Kafka
对于Kafka Producer和Consumer都需要以下几个配置：

|相关文件|参数|值|
|:--|:--|:--|
|kafka_client_jaas.conf|AccessKey|阿里云账号的AccessKey|
|kafka_client_jaas.conf|SecretKey|阿里云账号的SecretKey|
|producer/ReceiverConfig|ssl.truststore.location|{XXX，自己的路径}/kafka.client.truststore.jks|
|producer/Sender|topic|请修改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的ConsumerID|
|consumer/ReceiverConfig|groupId|请修改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的Topic(类型为Kafka消息)|

KafkaSaslConfig类可以不用，用来替代的是加入JVM启动参数
```
-Djava.security.auth.login.config=/home/admin/kafka_client_jaas.conf
```



	


