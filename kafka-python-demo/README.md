### demo说明
demo的目的仅仅是把应用跑起来作为参考，更多参数和程序健壮性请参考官方文档设置以保证客户端的稳定和性能。
相关文档请参考[kafka-python项目地址](https://github.com/dpkp/kafka-python)和[Kafka官网](https://kafka.apache.org/0100/documentation.html)

### demo分类

```
├── README.md
├── vpc
│   ├── aliyun_kafka_consumer.py
│   ├── aliyun_kafka_producer.py
│   └── setting.py
└── vpc-ssl
    ├── aliyun_kafka_consumer.py
    ├── aliyun_kafka_producer.py
    ├── ca-cert
    ├── setting.py
    └── setting.pyc
```

#### vpc
vpc实例接入demo
1. setting.py中的bootstrap_servers请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) 
2. Topic与CID请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6)，然后将其更新到setting.py中
3. pip install kafka-python


#### vpc-ssl
公网ssl实例接入demo
1. setting.py中的bootstrap_servers请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) 
2. Topic与CID请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6)，然后将其更新到setting.py中
3. setting.py的sasl_plain_usernamesasl_plain_password请从实例详情获取用户名和密码
4. pip install kafka-python
5. kafka-python这个库目前只支持SASL的PLAIN机制
6. ca-cert是aliyun提供的证书


