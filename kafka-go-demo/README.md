### 接入说明

1. 请先阅读[上一层目录的README](https://github.com/AliwareMQ/aliware-kafka-demos)
2. servers根据region进行选择，region列表可在上一层目录的README中找到
2. Topic(注意消息类型是"kafka消息")和CID申请成功后，将其更新到conf/mq.json中
3. 本Demo基于[开源 sarama GO客户端](https://github.com/Shopify/sarama)开发；虽然理论上来说任何支持SASL与SSL机制的Go客户端都可以接入进来，但该GO客户端已经在生产中得到充分验证，我们建议你使用它
4. Kafka默认的SASL机制是PLAIN，这种机制安全性虽然够高，但在每个连接建立的瞬间仍然有被破译的可能性；如果需要保证理论上的不可破译性，请参考[Java ONS机制](https://github.com/dongeforever/KafkaOnsDemo)；这里暂时没有该机制的实现代码，敬请期待
5. SASL的PLAIN机制，请配置ak为自己账号的AccessKey，password为**自己账号SecretKey的后10位**
6. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX)


