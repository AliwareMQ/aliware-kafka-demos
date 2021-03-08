### 准备客户端
本demo配置的目的仅仅是把应用跑起来作为参考，更多参数和程序健壮性请参考官方文档设置以保证客户端的稳定和性能。
本demo基于开源客户端进行编写，官网链接 https://github.com/confluentinc/confluent-kafka-go。

go 1.13+

### 准备配置
修改 conf/kafka.json 中的配置   
接入点参考 https://help.aliyun.com/document_detail/162334.html

| 配置项 | 说明 |  是否必须 |
| --- | --- | --- |
| topic | 控制台上申请的 Topic | 是
| group.id | 控制台上申请的 ConsumerGroup | 否，只有Consumer需要 |
| bootstrap.servers | 实例详情页查看接入点 | 是 |
| security.protocol | 参考文档选取，默认是plaintext | 是 |
| sasl.mechanism | 鉴权机制，默认是 PLAIN | 否 |
| sasl.username | 实例详情页获取 | 否 |
| sasl.password | 实例详情页获取 | 否 |

### 测试执行
```
# 发送消息
go run producer/producer.go
# 消费消息
go run consumer/consumer.go
```


