### 接入说明

1. 请先阅读[上一层目录的README](https://github.com/AliwareMQ/aliware-kafka-demos)
2. 遇到问题先参考[Kafka常见报错及排查](https://help.aliyun.com/document_detail/57058.html)
3. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX) 

### 配置说明

| demo中配置文件 | 配置项 | 说明 |
| --- | --- | --- |
| setting.php | topic_name | 请修改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的Topic(类型为Kafka消息) |
| setting.php | bootstrap_servers | 请根据[region列表](https://github.com/AliwareMQ/aliware-kafka-demos)进行选取 |
| setting.php  | sasl_plain_username | 请修改为阿里云账号的AccessKey |
| setting.php  | sasl_plain_password | 请修改为阿里云账号的SecretKey的后10位 |
| setting.php  | consumer_id | 请修改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的ConsumerID |








