### 接入说明
Demo 的目的仅仅是把应用跑起来作为参考，更多参数和程序健壮性请参考官方文档设置以保证客户端的稳定和性能。
相关资料请参考[开源源码](https://github.com/confluentinc/confluent-kafka-python)和[开源文档](https://docs.confluent.io/current/clients/confluent-kafka-python/).
更多设置参数参考[librdkafka参数设置](https://github.com/edenhill/librdkafka/blob/master/CONFIGURATION.md)

#### 接入步骤
1. setting.py中的bootstrap_servers请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) 
2. Topic与CID请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6)，然后将其更新到setting.py中
3. Username和Password请在控制台实例详情获取，设置到setting.py中
4. pip install confluent-kafka


