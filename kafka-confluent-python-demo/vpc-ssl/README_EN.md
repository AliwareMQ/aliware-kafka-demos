### Description
The demo aims to show you how to run an application. Refer to official documentation to configure parameters. This helps ensure the robustness of your application and the stability and performance of the client.
For more information, see [confluent-kafka-python](https://github.com/confluentinc/confluent-kafka-python) and [confluent_kafka API](https://docs.confluent.io/current/clients/confluent-kafka-python/).
For information about parameter configurations, see [Configuration properties](https://github.com/edenhill/librdkafka/blob/master/CONFIGURATION.md).

#### Procedure
1. For information about endpoints in setting.py, see [View endpoints](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga).
2. For information about topics and consumer groups, see [Create resources](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6). Then, update the resource information in setting.py.
3. Configure the username and password in setting.py after you obtain them on the Instance Details page in the Message Queue for Apache Kafka console.
4. pip install confluent-kafka