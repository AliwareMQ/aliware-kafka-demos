### 接入说明

1. 本Demo基于[开源Node rdkafka客户端](https://github.com/Blizzard/node-rdkafka)开发；
2. 按照下面的内容说明配置和运行Demo
3. 遇到问题先参考[Kafka常见报错及排查](https://help.aliyun.com/document_detail/57058.html)
4. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX) 

## 运行Demo
1. 请确保安装了Node环境
2. export LDFLAGS="-L/usr/local/opt/openssl/lib"; export CPPFLAGS="-I/usr/local/opt/openssl/include"; npm install node-rdkafka
3. 按照本页下面配置说明配置producer.js与consumer.js
4. 生产: node producer.js
5. 消费: node consumer.js

### 配置说明

| demo中配置文件 | 配置项 | 说明 |
| --- | --- | --- |
| producer.js/consumer.js | topic | 请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6) |
| producer.js/consumer.js | bootstrap.servers | 请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) |
| producer.js/consumer.js  | sasl.username | 请修改为阿里云账号的AccessKey |
| producer.js/consumer.js  | sasl.password | 请修改为阿里云账号的SecretKey的后10位 |
| consumer.js  | group.id | 请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6) |
| producer.js/consumer.js  | ssl.ca.location | 根证书路径，运行Demo时无需修改，实际部署时注意路径 |





