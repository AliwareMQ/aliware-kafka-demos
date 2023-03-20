### Description
The demo aims to show you how to run an application. Refer to official documentation to configure parameters. This helps ensure the robustness of your application and the stability and performance of the client.
1. This demo is developed based on the open source client [node-rdkafka](https://github.com/Blizzard/node-rdkafka).
2. Follow the instructions below to configure and run the demo.

### Run the demo
1. Make sure that the Node environment is installed.
2. export LDFLAGS="-L/usr/local/opt/openssl/lib"; export CPPFLAGS="-I/usr/local/opt/openssl/include"; npm install node-rdkafka
3. Follow the instructions below to configure the producer.js and consumer.js files.
4. Run the node producer.js file to produce messages.
5. Run the node consumer.js file to consume messages.

### Configuration description

##### vpc
Demo for the access of VPC-connected instances

| Configuration file in demo | Parameter | Description |
| --- | --- | --- |
| producer.js/consumer.js | topic | The name of the topic that you created in the Message Queue for Apache Kafka console. For more information, see [Create resources](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6). |
| producer.js/consumer.js | bootstrap.servers | The endpoint. For more information, see [View endpoints](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga). |
| consumer.js | group.id | The ID of the consumer group that you created in the Message Queue for Apache Kafka. For more information, see [Create resources](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6). |

##### vpc-ssl
Note that the public bandwidth must not be too small. Otherwise, messages may fail to be read. Refer to the settings of parameters such as max.partition.fetch.bytes in consumer.js. It is recommended that the following conditions be met:

* Set the max.partition.fetch.bytes parameter to a value that is greater than the size of a message.
* Set the max.partition.fetch.bytes \* parameter to a value that is less than Public bandwidth/8. Unit for public bandwidth: bit/s. Unit for the parameter: bytes.


Demo for the access of Internet-connected instances by using the SSL endpoint

| Configuration file in demo | Parameter | Description |
| --- | --- | --- |
| producer.js/consumer.js | topic | The name of the topic that you created in the Message Queue for Apache Kafka console. For more information, see [Create resources](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6). |
| producer.js/consumer.js | bootstrap.servers | The endpoint. For more information, see [View endpoints](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga). |
| producer.js/consumer.js | sasl.username | The username. Use the username on the Instance Details page in the Message Queue for Apache Kafka console. |
| producer.js/consumer.js | sasl.password | The password. Use the password on the Instance Details page in the Message Queue for Apache Kafka console. |
| consumer.js | group.id | The ID of the group that you created in the Message Queue for Apache Kafka console. For more information, see [Create resources](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6). |
| producer.js/consumer.js | ssl.ca.location | The path of the root certificate. You do not need to modify the path when you run the demo. However, during actual deployment, you must change the path to the actual path. |


