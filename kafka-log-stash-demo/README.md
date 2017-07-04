### 接入说明
1. 请先阅读[上一层目录的README](https://github.com/AliwareMQ/aliware-kafka-demos)
2. Topic和CID申请成功后，将其更新到`agent/logstash.conf`中（对应字段为topics和group_id）；
3. 将自己的AccessKey，SecretKey更新到`agent/jaas.conf`中
4. `agent/logstash.conf`中bootstrap_servers请根据region列表进行选取
5. 云上Kafka用SASL/ONS模式进行鉴权，详细配置参考：`logstash.conf`，需要注意的是需要将`ons-sasl-client-0.1.jar`包拷贝到logstash的kafka-input-plugin目录下面去，不然用ONS模式进行鉴权的时候会找不到该包
6. 仔细阅读`logstash.conf`, 涉及到路径的（jaas_path，ssl_truststore_location）请修改成自己的路径，并将对应文件拷贝到相应目录
7. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX)


## 参考文档：
[上一层目录的README](https://github.com/AliwareMQ/aliware-kafka-demos) 
https://www.elastic.co/guide/en/logstash/current/plugins-inputs-kafka.html

