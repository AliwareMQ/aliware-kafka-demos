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








