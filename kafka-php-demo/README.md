
### 重要建议
PHP客户建议使用[MNS](https://www.aliyun.com/product/mns)的HTTP协议接入。
主要原因是：

* Kafka是一个长连接的服务，频繁的创建连接和关闭连接会影响服务端的稳定性，因此服务端对单客户机器IP的连接数上限做了严格限制，目前是不得超过50；
* PHP有一个固有的问题，难以做到连接共享，每发一条消息，创建一个Producer，也即创建新连接，是一种效率极低的方式，会造成延迟很大，效率很低；

综上，PHP客户建议使用MNS。


### 接入说明
1. 请先阅读[上一层目录的README](https://github.com/AliwareMQ/aliware-kafka-demos)
2. 遇到问题先参考[Kafka常见报错及排查](https://help.aliyun.com/document_detail/57058.html)
3. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX) 

### 运行Demo
1. 本Demo基于开源客户端[php-rdkafka](https://github.com/arnaud-lb/php-rdkafka), 请先按照[installation guide](https://arnaud-lb.github.io/php-rdkafka/phpdoc/rdkafka.setup.html)安装相应模块（mac下可用brew安装，安装完后可能需要配置下php.ini），安装成功后调用php -m|grep kafka确认下
2. 按照下面的配置说明，配置setting.php
3. php kafka-producer.php  发送消息
4. php kafka-consumer.php  消费消息


### 配置说明

| demo中配置文件 | 配置项 | 说明 |
| --- | --- | --- |
| setting.php | topic_name | 请修改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的Topic(类型为Kafka消息) |
| setting.php | bootstrap_servers | 请根据[region列表](https://github.com/AliwareMQ/aliware-kafka-demos)进行选取 |
| setting.php  | sasl_plain_username | 请修改为阿里云账号的AccessKey |
| setting.php  | sasl_plain_password | 请修改为阿里云账号的SecretKey的后10位 |
| setting.php  | consumer_id | 请修改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的ConsumerID |








