### 接入说明
用户可以利用Kafka Python Client去收发Ons的消息，使用之前请先阅读以下规约：
1. 使用之前请先阅读[上一层目录的README](https://github.com/AliwareMQ/aliware-kafka-demos)
2. setting.py中的bootstrap_servers请根据region列表进行选择
2. Topic(注意消息类型是"kafka消息")与CID申请成功后，将其更新到setting.py中；
3. setting.py的sasl_plain_username即为阿里云的AccessKey，sasl_plain_password为阿里云的**SecretKey的后10位**
4. pip install kafka-python
5. kafka-python这个库目前只支持SASL的PLAIN机制
6. ca-cert是aliyun提供的证书
7. 其它Kafka使用相关文档请参考[kafka-python项目地址](https://github.com/dpkp/kafka-python)和[Kafka官网](https://kafka.apache.org/0100/documentation.html)
8. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX)  


