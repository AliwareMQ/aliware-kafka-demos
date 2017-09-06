### 接入说明

1. 请先阅读[上一层目录的README](https://github.com/AliwareMQ/aliware-kafka-demos)
2. 本Demo基于[开源Node rdkafka客户端](https://github.com/Blizzard/node-rdkafka)开发；
3. 按照下面的内容说明配置和运行Demo
4. 遇到问题先参考[Kafka常见报错及排查](https://help.aliyun.com/document_detail/57058.html)
5. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX) 

## 运行Demo
1. 请确保安装了Node环境
2. export LDFLAGS="-L/usr/local/opt/openssl/lib"; export CPPFLAGS="-I/usr/local/opt/openssl/include"; npm install node-rdkafka
3. 按照本页下面配置说明配置producer.js与consumer.js
4. 生产: node producer.js
5. 消费: node consumer.js

### 配置说明

| demo中配置文件 | 配置项 | 说明 |
| --- | --- | --- |
| producer.js/consumer.js | topic | 请改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的Topic(类别Kafka消息) |
| producer.js/consumer.js | bootstrap.servers | 请根据[region列表](https://github.com/AliwareMQ/aliware-kafka-demos)进行选取 |
| producer.js/consumer.js  | sasl.username | 请修改为阿里云账号的AccessKey |
| producer.js/consumer.js  | sasl.password | 请修改为阿里云账号的SecretKey的后10位 |
| consumer.js  | group.id | 请修改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的ConsumerID |
| producer.js/consumer.js  | ssl.ca.location | 根证书路径，运行Demo时无需修改，实际部署时注意路径 |


### 特别说明
阿里云Kafka服务会主动掐掉空闲连接。Node.js客户端无法自适应该问题，需要手动干预。
如果遇到该情况，主动disconnect再connect可以规避此类问题（请参考producer.js）。
本Demo代码，仅作为Demo使用，实际生产时请参考该Demo进行调试。








