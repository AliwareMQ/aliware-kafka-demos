### 接入说明
1. 云上Kafka用SASL/ONS模式进行鉴权，需要注意的是需要将agent目录下面的jar包拷贝到logstash的kafka-input-plugin/kafka-output-plugin目录下面的相关jar目录(目录示例，~logstash/logstash-5.4.1/vendor/bundle/jruby/1.9/gems/logstash-output-kafka-5.1.6/vendor/jar-dependencies/runtime-jars/, ~logstash/logstash-5.4.1/vendor/bundle/jruby/1.9/gems/logstash-input-kafka-5.1.6/vendor/jar-dependencies/runtime-jars/，具体版本请参考相关文档)，不然用ONS模式进行鉴权的时候会找不到对应包
2. 请仔细阅读下面的配置表，并修改为自己对应的配置
3. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX)

### 配置列表

| demo中配置文件 | 配置项 | 说明 |
| --- | --- | --- |
| agent/jaas.conf | AccessKey | 请修改为阿里云账号的AccessKey |
| agent/jaas.conf | SecretKey | 请修改为阿里云账号的SecretKey |
| agent/logstash.conf | bootstrap_servers | 请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) |
| agent/logstash.conf | topics | 请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6) |
| agent/logstash.conf | group_id | 请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6) |
| agent/logstash.conf | security_protocol | SASL_SSL，无需修改 |
| agent/logstash.conf | sasl_mechanism | ONS，无需修改 |
| agent/logstash.conf | jaas_path | 请修改成agent/jaas.conf的存放位置 |
| agent/logstash.conf | ssl_truststore_location | 请修改成agent/kafka.client.truststore.jks存放的位置 |
| agent/logstash.conf | ssl_truststore_password | KafkaOnsClient，无需修改 |


## 参考文档： 
[plugins-inputs-kafka](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-kafka.html)  
[plugins-outputs-kafka](https://www.elastic.co/guide/en/logstash/current/plugins-outputs-kafka.html)






