### 接入说明
demo配置的目的仅仅是把应用跑起来作为参考，更多参数和程序健壮性请参考官方文档设置以保证客户端的稳定和性能。
相关参考文档： 
[plugins-inputs-kafka](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-kafka.html)  
[plugins-outputs-kafka](https://www.elastic.co/guide/en/logstash/current/plugins-outputs-kafka.html)


### 配置列表

##### beta(废弃)

##### vpc
vpc实例接入demo配置

| demo中配置文件 | 配置项 | 说明 |
| --- | --- | --- |
| agent/logstash.conf | bootstrap_servers | 请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) |
| agent/logstash.conf | topics | 请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6) |
| agent/logstash.conf | group_id | 请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6) |

##### vpc-ssl
公网ssl实例接入demo配置

| demo中配置文件 | 配置项 | 说明 |
| --- | --- | --- |
| agent/jaas.conf | username | 请修改实例详情中的用户名 |
| agent/jaas.conf | password | 请修改实例详情中的密码 |
| agent/logstash.conf | bootstrap_servers | 请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) |
| agent/logstash.conf | topics | 请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6) |
| agent/logstash.conf | group_id | 请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6) |
| agent/logstash.conf | security_protocol | SASL_SSL，无需修改 |
| agent/logstash.conf | sasl_mechanism | PLAIN，无需修改 |
| agent/logstash.conf | jaas_path | 请修改成agent/jaas.conf的存放位置 |
| agent/logstash.conf | ssl_truststore_location | 请修改成agent/kafka.client.truststore.jks存放的位置 |
| agent/logstash.conf | ssl_truststore_password | KafkaOnsClient，无需修改 |





