### 接入说明
Demo 的目的仅仅是把应用跑起来作为参考，更多参数和程序健壮性请参考官方文档设置以保证客户端的稳定和性能。
相关资料请参考[开源文档](https://docs.confluent.io/current/kafka-rest/docs/quickstart.html)

#### 接入步骤
1. bootstrap.servers请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) 
2. Topic与CID请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6)。


#### API

```
# Get a list of topics
$ curl "http://localhost:8082/topics"
  ["__consumer_offsets","_schemas","avrotest","binarytest","jsontest"]

# Produce a message using Avro embedded data, including the schema which will
# be registered with schema registry and used to validate and serialize
# before storing the data in Kafka
$ curl -X POST -H "Content-Type: application/vnd.kafka.avro.v2+json" \
      -H "Accept: application/vnd.kafka.v2+json" \
      --data '{"value_schema": "{\"type\": \"record\", \"name\": \"User\", \"fields\": [{\"name\": \"name\", \"type\": \"string\"}]}", "records": [{"value": {"name": "testUser"}}]}' \
      "http://localhost:8082/topics/avrotest"

# You should get the following response:
  {"offsets":[{"partition":0,"offset":0,"error_code":null,"error":null}],"key_schema_id":null,"value_schema_id":21}

# Produce a message with Avro key and value.
# Note that if you use Avro values you must also use Avro keys, but the schemas can differ

$ curl -X POST -H "Content-Type: application/vnd.kafka.avro.v2+json" \
      -H "Accept: application/vnd.kafka.v2+json" \
      --data '{"key_schema": "{\"name\":\"user_id\"  ,\"type\": \"int\"   }", "value_schema": "{\"type\": \"record\", \"name\": \"User\", \"fields\": [{\"name\": \"name\", \"type\": \"string\"}]}", "records": [{"key" : 1 , "value": {"name": "testUser"}}]}' \
      "http://localhost:8082/topics/avrokeytest2"

# You should get the following response:
{"offsets":[{"partition":0,"offset":0,"error_code":null,"error":null}],"key_schema_id":2,"value_schema_id":1}

# Create a consumer for Avro data, starting at the beginning of the topic's
# log and subscribe to a topic. Then consume some data from a topic, which is decoded, translated to
# JSON, and included in the response. The schema used for deserialization is
# fetched automatically from schema registry. Finally, clean up.
$ curl -X POST  -H "Content-Type: application/vnd.kafka.v2+json" \
      --data '{"name": "my_consumer_instance", "format": "avro", "auto.offset.reset": "earliest"}' \
      http://localhost:8082/consumers/my_avro_consumer

  {"instance_id":"my_consumer_instance","base_uri":"http://localhost:8082/consumers/my_avro_consumer/instances/my_consumer_instance"}

$ curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" --data '{"topics":["avrotest"]}' \
 http://localhost:8082/consumers/my_avro_consumer/instances/my_consumer_instance/subscription
 # No content in response

$ curl -X GET -H "Accept: application/vnd.kafka.avro.v2+json" \
      http://localhost:8082/consumers/my_avro_consumer/instances/my_consumer_instance/records
  [{"key":null,"value":{"name":"testUser"},"partition":0,"offset":1,"topic":"avrotest"}]


$ curl -X DELETE -H "Content-Type: application/vnd.kafka.v2+json" \
      http://localhost:8082/consumers/my_avro_consumer/instances/my_consumer_instance
# No content in response
```





