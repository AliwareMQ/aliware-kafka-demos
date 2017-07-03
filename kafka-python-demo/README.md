### 接入说明
用户可以利用Kafka Python Client去收发Ons的消息，使用之前请先阅读以下规约：
1. 使用之前需要先申请Topic(类型选择Kafka消息)与Consumer ID，详情请参考[申请MQ资源](https://help.aliyun.com/document_detail/29536.html?spm=5176.doc29546.2.2.gWIToO)
2. 如果没有申请Topic和Consumer ID，则会直接导致鉴权失败；申请成功后，将其更新到setting.py中；
3. setting.py的sasl_plain_username即为阿里云的AccessKey，sasl_plain_password在公测期间需要加入钉钉群找管理员配置；
4. pip install kafka-python
5. kafka-python这个库目前只支持SASL的PLAIN机制
6. ca-cert是aliyun提供的证书
7. 其它Kafka使用相关文档请参考[Kafka官网](https://kafka.apache.org/0100/documentation.html)
8. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX)  


