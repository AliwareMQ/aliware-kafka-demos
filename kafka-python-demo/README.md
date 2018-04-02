# ### 接入说明
用户可以利用Kafka Python Client去收发Ons的消息，使用之前请先阅读以下规约：
1. setting.py中的bootstrap_servers请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) 
2. Topic与CID请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6)，然后将其更新到setting.py中；
3. setting.py的sasl_plain_username即为阿里云的AccessKey，sasl_plain_password为阿里云的**SecretKey的后10位**
4. pip install kafka-python
5. kafka-python这个库目前只支持SASL的PLAIN机制
6. ca-cert是aliyun提供的证书
7. 其它Kafka使用相关文档请参考[kafka-python项目地址](https://github.com/dpkp/kafka-python)和[Kafka官网](https://kafka.apache.org/0100/documentation.html)
8. 遇到问题先参考[Kafka常见报错及排查](https://help.aliyun.com/document_detail/57058.html)


