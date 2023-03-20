### Run the demo
1. Refer to the CPP demo to install the Kafka library first.
2. This Demo is based on the open source client [php-rdkafka](https://github.com/arnaud-lb/php-rdkafka). Install the corresponding module based on the [installation guide](https://arnaud-lb.github.io/php-rdkafka/phpdoc/rdkafka.setup.html) first. You may need to configure php.ini after you install the module. To confirm whether the installation is successful, call php -m|grep kafka.
3. Configure setting.php based on the following instructions.
4. Modify other configurations in the code. Example: `$conf->set('xxx', 'xxx');`. For information about the description of the configurations, see [Configuration properties](https://github.com/edenhill/librdkafka/blob/master/CONFIGURATION.md).
5. Run the php kafka-producer.php file to send messages.
6. Run the php kafka-consumer.php file to consume messages.


### Configuration description

| Configuration file in demo | Parameter | Description |
| --- | --- | --- |
| setting.php | topic_name | The name of the topic that you created in the Message Queue for Apache Kafka console. For more information, see [Create resources](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6). |
| setting.php | consumer_id | The ID of the consumer group that you created in the Message Queue for Apache Kafka console. For more information, see [Create resources](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6). |
| setting.php | bootstrap_servers | The endpoint. For more information, see [View endpoints](https://help.aliyun.com/document_detail/68342.html). |
| setting.php | sasl_plain_username | The username. You can obtain the username on the Instance Details page in the Message Queue for Apache Kafka console. |
| setting.php | sasl_plain_password | The password. You can obtain the password on the Instance Details page in the Message Queue for Apache Kafka console. |








