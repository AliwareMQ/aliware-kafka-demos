### Prepare the client
The demo aims to show you how to run an application. Refer to official documentation to configure parameters. This helps ensure the robustness of your application and the stability and performance of the client.
The demo is written based on open source clients. For more information, see https://github.com/confluentinc/confluent-kafka-go.

go 1.13+

### Prepare configuration files
Modify the     configuration in conf/kafka.json.
For information about endpoints, see https://www.alibabacloud.com/help/en/message-queue-for-apache-kafka/latest/comparison-among-endpoints.
Multiple topics are sent in the demo. Therefore, create at least two topics in the Message Queue for Apache Kafka console before the test.

| Parameter | Description | Required |
| --- | --- | --- |
| topic | The name of one of the topics that you created in the Message Queue for Apache Kafka console. | Yes |
| topic2 | The name of the other topic that you created in the Message Queue for Apache Kafka console. | Yes |
| group.id | The ID of the consumer group that you created in the Message Queue for Apache Kafka console. | No. Consumer groups are required only by consumers. |
| bootstrap.servers | The endpoint. You can obtain the endpoint on the Instance Details page in the Message Queue for Apache Kafka console. | Yes |
| security.protocol | The security protocol. Default value: PLAINTEXT. Valid values: SASL_SSL and SASL_PLAINTEXT. | Yes |
| sasl.mechanism | The Simple Authentication and Security Layer (SASL) mechanism. Default value: PLAIN. Valid value: SCRAM-SHA-256. | Required if SASL is included in the protocol. |
| sasl.username | The username. You can obtain the username on the Instance Details page in the Message Queue for Apache Kafka console. | Required if SASL is included in the protocol. |
| sasl.password | The password. You can obtain the password on the Instance Details page in the Message Queue for Apache Kafka console. | Required if SASL is included in the protocol. |

### Start the test
```
# Send messages
go run -mod=vendor producer/producer.go
# Consume messages
go run -mod=vendor consumer/consumer.go
```


