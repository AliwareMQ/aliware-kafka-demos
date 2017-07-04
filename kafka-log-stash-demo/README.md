### 接入说明
1. 请先阅读[上一层目录的README](https://github.com/AliwareMQ/aliware-kafka-demos)
2. 云上Kafka用SASL/ONS模式进行鉴权，需要注意的是需要将`ons-sasl-client-0.1.jar`包拷贝到logstash的kafka-input-plugin/kafka-output-plugin目录下面去，不然用ONS模式进行鉴权的时候会找不到该包
3. 请仔细阅读下面的配置表，并修改为自己对应的配置
4. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX)

### 配置列表

| demo中配置文件 | 配置项 | 说明 |
| --- | --- | --- |
| agent/jaas.conf | AccessKey | 请修改为阿里云账号的AccessKey |
| agent/jaas.conf | SecretKey | 请修改为阿里云账号的SecretKey |
| agent/logstash.conf | bootstrap_servers | 请根据[region列表](https://github.com/AliwareMQ/aliware-kafka-demos)进行选取 |
| agent/logstash.conf | topics | 请修改为MQ控制台上申请的Topic |
| agent/logstash.conf | group_id | 请修改为MQ控制台申请的CID |
| agent/logstash.conf | security_protocol | SASL_SSL，无需修改 |
| agent/logstash.conf | sasl_mechanism | ONS，无需修改 |
| agent/logstash.conf | jaas_path | 请修改成agent/jaas.conf的存放位置 |
| agent/logstash.conf | ssl_truststore_location | 请修改成agent/kafka.client.truststore.jks存放的位置 |
| agent/logstash.conf | ssl_truststore_password | KafkaOnsClient，无需修改 |


## 参考文档： 
[plugins-inputs-kafka](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-kafka.html)  
[plugins-outputs-kafka](https://www.elastic.co/guide/en/logstash/current/plugins-outputs-kafka.html)






