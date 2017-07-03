### 接入说明

	用户可以利用LogStash接入云上Kafka，使用之前请先阅读以下规约：
1. 使用之前需要先申请Topic(类型选择Kafka消息)与Consumer ID，详情请参考[申请MQ资源](https://help.aliyun.com/document_detail/29536.html?spm=5176.doc29546.2.2.gWIToO)
2. 将自己的AccessKey，SecretKey更新到`agent/jaas.conf`中
3. 如果没有申请Topic和Consumer ID，则会直接导致鉴权失败；申请成功后，将其更新到`agent/logstash.conf`中（对应字段为topics和group_id）；
4. 云上Kafka用SASL/ONS模式进行鉴权，详细配置参考：`logstash.conf`，需要注意的是需要将`ons-sasl-client-0.1.jar`包拷贝到logstash的kafka-input-plugin目录下面去，不然用ONS模式进行鉴权的时候会找不到该包
5. 仔细阅读`logstash.conf`, 涉及到路径的（jaas_path，ssl_truststore_location）请修改成自己的路径，并将对应文件拷贝到相应目录
6. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX)


## 参考文档：
https://github.com/dongeforever/KafkaOnsDemo  
https://www.elastic.co/guide/en/logstash/current/plugins-inputs-kafka.html

