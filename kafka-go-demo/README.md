### 接入说明

1. 请先阅读[上一层目录的README](https://github.com/AliwareMQ/aliware-kafka-demos)
2. 本Demo基于[开源 sarama GO客户端](https://github.com/Shopify/sarama)开发；虽然理论上来说任何支持SASL与SSL机制的Go客户端都可以接入进来，但该GO客户端已经在生产中得到充分验证，我们建议你使用它
3. 按照下面的内容说明配置和运行Demo
4. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX)

## 运行Demo
1. 请确保安装了Go环境，详情参考https://golang.org
2. git clone https://github.com/AliwareMQ/aliware-kafka-demos.git
3. export GOPATH=`pwd`"/aliware-kafka-demos/kafka-go-demo"
4. cd aliware-kafka-demos/kafka-go-demo
5. 安装依赖(请保证联网，需要一定时间，请耐心等待): go get github.com/Shopify/sarama/ ; go get github.com/bsm/sarama-cluster
6. go install services
7. go install services/producer/
8. go install services/consumer
9. 按照本页下面配置说明配置conf/mq.json
10. 生产: ./bin/producer
11. 消费：./bin/consumer

### 配置说明

| demo中配置文件 | 配置项 | 说明 |
| --- | --- | --- |
| conf/mq.json | topics | 请修改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的Topic(类型为Kafka消息) |
| conf/mq.json | servers | 请根据[region列表](https://github.com/AliwareMQ/aliware-kafka-demos)进行选取 |
| conf/mq.json  | ak | 请修改为阿里云账号的AccessKey |
| conf/mq.json  | password | 请修改为阿里云账号的SecretKey的后10位 |
| conf/mq.json  | consumerId | 请修改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的ConsumerID |
| conf/mq.json  | cert_file | 根证书路径，运行Demo时无需修改，实际部署时注意相对路径 |








