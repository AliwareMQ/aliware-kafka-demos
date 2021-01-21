### demo说明
demo的目的仅仅是把应用跑起来作为参考，更多参数和程序健壮性请参考官方文档设置以保证客户端的稳定和性能。
相关文档请参考[filebeat-kafka](https://www.elastic.co/guide/en/beats/filebeat/current/kafka-output.html)和[Kafka官网](https://kafka.apache.org/0100/documentation.html)
  

### demo分类
```
├── README.md
├── vpc
│   └── output.kafka.conf
└── vpc-ssl
    ├── ca-cert
    └── output.kafka.conf
```

#### vpc
vpc实例接入demo
1. output.kafka.conf中的hosts请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) 
2. topic请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6)
3. 设置版本version，0.10.x系列设置"0.10.2", 2.x系列设置"2.0.0"

#### vpc-ssl
公网ssl实例接入demo
1. output.kafka.conf中的hosts请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) 
2. topic请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6)
3. 控制台实例信息获取用户名和密码username和password
4. 设置版本version，0.10.x系列设置"0.10.2", 2.x系列设置"2.0.0"
5. 配置数字证书和设置校验模式 
