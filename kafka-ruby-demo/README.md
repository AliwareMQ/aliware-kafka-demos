### 接入说明
Demo 的目的仅仅是把应用跑起来作为参考，更多参数和程序健壮性请参考官方文档设置以保证客户端的稳定和性能。
相关资料请参考[开源源码](https://github.com/zendesk/ruby-kafka
).


#### 接入步骤
1. gem install ruby-kafka -v 0.6.8
2. brokers 请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) 
3. topic 与 consumerGroup 请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6)

#### vpc-ssl
1. brokers(SSL接入点) 请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) 
2. topic 与 consumerGroup 请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6)
3. username,password请从实例详情获取用户名和密码
4. cert.pem是aliyun提供的证书