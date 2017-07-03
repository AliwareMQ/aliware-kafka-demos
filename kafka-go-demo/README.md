### 接入说明

	用户可以利用Kafka Go Client去收发Ons的消息，使用之前请先阅读以下规约：
1. 使用之前需要先申请Topic(类型选择Kafka消息)与Consumer ID，详情请参考[申请MQ资源](https://help.aliyun.com/document_detail/29536.html?spm=5176.doc29546.2.2.gWIToO)
2. 如果没有申请Topic和Consumer ID，则会直接导致鉴权失败；申请成功后，将其更新到conf/mq.json中；
3. 本Demo基于[开源 sarama GO客户端](https://github.com/Shopify/sarama)开发；虽然理论上来说任何支持SASL与SSL机制的Go客户端都可以接入进来，但该GO客户端已经在生产中得到充分验证，我们建议你使用它；
4. Kafka默认的SASL机制是PLAIN，这种机制安全性虽然够高，但在每个连接建立的瞬间仍然有被破译的可能性；如果需要保证理论上的不可破译性，请参考[Java ONS机制](https://github.com/dongeforever/KafkaOnsDemo)；这里暂时没有该机制的实现代码，敬请期待；
5. 如果使用SASL的PLAIN机制，请加入钉钉群，找工作人员配置AK与密码，并修改conf/mq.json
6. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX)


