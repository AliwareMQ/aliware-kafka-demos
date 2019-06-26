## Demo简介

本demo演示如何使用[Confluent Schema Registry](https://docs.confluent.io/3.0.1/schema-registry/docs/index.html)连接阿里云[消息队列 KAFKA](https://kafka.console.aliyun.com)。关于Confluent schema registry原理以及使用场景，请自行参考[官方文档](https://docs.confluent.io/3.0.1/schema-registry/docs/index.html)。

## 服务端版本

要求服务端版本2.0以上。

## 获取Confluent Schema Registry

两种获取方式：
1. 下载[Confluent Platform](https://www.confluent.io/download/?_ga=2.122298381.704320543.1561476382-1283465873.1561476382)，根据官方[Quick Start](https://docs.confluent.io/3.0.1/quickstart.html#quickstart)完成安装；
2. 从github上下载[Schema Registry源码](https://github.com/confluentinc/schema-registry)，本地自己编译安装。注意，构建Schema Registry依赖[common](https://github.com/confluentinc/common)和[rest-utils](https://github.com/confluentinc/rest-utils)，需提前将common和rest-utils构建好。详细步骤可以参见Schema Registry源码的README.md，这里不再详述。

## 启动Confluent Schema Registry

获取并构建完Schema Registry之后，开始进行配置。

### 配置

#### VPC接入
编辑./config/schema-registry.properties文件
```
## 监听本机8081端口
listeners=http://0.0.0.0:8081

## Kafka接入点，通过控制台获取
## 您在控制台获取的默认接入点
kafkastore.bootstrap.servers=控制台上的接入点IP+Port

## Schema将保存在这个topic中，需要在控制台提前创建好。
## 要求为local存储，日志清理策略为compact，且分区数为1。
kafkastore.topic=schemasTopic

## Schema Registry内部需要用到的consumer group，提前在控制台创建好
schema.registry.group.id=schemas_registry_1

```

#### 公网接入

1. 编辑./config/schema-registry.properties文件

```
## 监听本机8081端口
listeners=http://0.0.0.0:8081

## Kafka接入点，通过控制台获取
## 您在控制台获取的默认接入点
kafkastore.bootstrap.servers=控制台上的接入点IP+Port

## Schema将保存在这个topic中，需要在控制台提前创建好。
## 要求为local存储，日志清理策略为compact，且分区数为1。
kafkastore.topic=schemasTopic

## Schema Registry内部需要用到的consumer group，提前在控制台创建好
schema.registry.group.id=schemas_registry_1

## 如果是公网接入，需要配置以下5项
kafkastore.ssl.truststore.location=/xxx/xxx/kafka.client.truststore.jks
kafkastore.ssl.truststore.password=KafkaOnsClient
kafkastore.security.protocol=SASL_SSL
kafkastore.sasl.mechanism=PLAIN
kafkastore.ssl.endpoint.identification.algorithm=

```
2. 编辑kafka_client_jaas.conf文件。

```
KafkaClient {
   org.apache.kafka.common.security.plain.PlainLoginModule required
   ## 控制台中实例的用户名和密码
   username="your username"
   password="your password";
};
```

### 启动

```
./bin/schema-registry-start ./etc/schema-registry/schema-registry.properties
```

注意，如果是公网接入需要配置JVM参数
```
-Djava.security.auth.login.config=/xxx/xxx/kafka_client_jaas.conf
```

## 验证Confluent Schema Registry启动成功

启动成功后，可以在控制台尝试以下操作。操作成功后，可以在控制台使用消息查询功能，查看schemasTopic中的Schema信息。
```
# Register a new version of a schema under the subject "Kafka-key"
$ curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"schema": "{\"type\": \"string\"}"}' \
    http://localhost:8081/subjects/Kafka-key/versions
  {"id":1}

# Register a new version of a schema under the subject "Kafka-value"
$ curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"schema": "{\"type\": \"string\"}"}' \
     http://localhost:8081/subjects/Kafka-value/versions
  {"id":1}

# List all subjects
$ curl -X GET http://localhost:8081/subjects
  ["Kafka-value","Kafka-key"]

# List all schema versions registered under the subject "Kafka-value"
$ curl -X GET http://localhost:8081/subjects/Kafka-value/versions
  [1]

# Fetch a schema by globally unique id 1
$ curl -X GET http://localhost:8081/schemas/ids/1
  {"schema":"\"string\""}

# Fetch version 1 of the schema registered under subject "Kafka-value"
$ curl -X GET http://localhost:8081/subjects/Kafka-value/versions/1
  {"subject":"Kafka-value","version":1,"id":1,"schema":"\"string\""}

# Fetch the most recently registered schema under subject "Kafka-value"
$ curl -X GET http://localhost:8081/subjects/Kafka-value/versions/latest
  {"subject":"Kafka-value","version":1,"id":1,"schema":"\"string\""}

# Delete version 3 of the schema registered under subject "Kafka-value"
$ curl -X DELETE http://localhost:8081/subjects/Kafka-value/versions/3
  3

# Delete all versions of the schema registered under subject "Kafka-value"
$ curl -X DELETE http://localhost:8081/subjects/Kafka-value
  [1, 2, 3, 4, 5]

# Check whether a schema has been registered under subject "Kafka-key"
$ curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"schema": "{\"type\": \"string\"}"}' \
    http://localhost:8081/subjects/Kafka-key
  {"subject":"Kafka-key","version":1,"id":1,"schema":"\"string\""}

# Test compatibility of a schema with the latest schema under subject "Kafka-value"
$ curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"schema": "{\"type\": \"string\"}"}' \
    http://localhost:8081/compatibility/subjects/Kafka-value/versions/latest
  {"is_compatible":true}

# Get top level config
$ curl -X GET http://localhost:8081/config
  {"compatibilityLevel":"BACKWARD"}

# Update compatibility requirements globally
$ curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"compatibility": "NONE"}' \
    http://localhost:8081/config
  {"compatibility":"NONE"}

# Update compatibility requirements under the subject "Kafka-value"
$ curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"compatibility": "BACKWARD"}' \
    http://localhost:8081/config/Kafka-value
  {"compatibility":"BACKWARD"}
```

## 发送消息

以java语言发送消息为例，详情参见[kafka-java-demo](https://github.com/AliwareMQ/aliware-kafka-demos/tree/master/kafka-java-demo)。
在kafka-java-demo基础上，我们需要做以下额外配置：

1. 添加avro依赖

```
<dependency>
    <groupId>io.confluent</groupId>
    <artifactId>kafka-avro-serializer</artifactId>
    <version>3.3.0</version>
</dependency>
```

2. 配置schema

```
public static final String USER_SCHEMA = "{\"type\":\"record\",\"name\":\"myrecord\",\"fields\":[{\"name\":\"f1\",\"type\":\"string\"}]}";
```

3. 配置序列化类，以value为例

```
// 使用KafkaAvroSerializer序列化消息的value
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");

// schema registry 接入url，即我们在上面步骤中启动的Confluent Schema Registry服务端http接入点
props.put("schema.registry.url", "http://127.0.0.1:8081");

// value使用第2步的USER_SCHEMA
Schema.Parser parser = new Schema.Parser();
Schema schema = parser.parse(USER_SCHEMA);
```

4. 发送消息

```
GenericRecord value = new GenericData.Record(schema);
value.put("f1", "test");
ProducerRecord<String, GenericRecord>  kafkaMessage =  new ProducerRecord<String, GenericRecord>(topic, value);
Future<RecordMetadata> metadataFuture = producer.send(kafkaMessage);
```

完整发送示例：

```
public class KafkaProducerDemo {

    public static final String USER_SCHEMA = "{\"type\":\"record\",\"name\":\"myrecord\",\"fields\":[{\"name\":\"f1\",\"type\":\"string\"}]}";

    public static void main(String args[]) {

        //加载kafka.properties
        Properties kafkaProperties = JavaKafkaConfigurer.getKafkaProperties();

        Properties props = new Properties();
        //设置接入点，请通过控制台获取对应Topic的接入点
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getProperty("bootstrap.servers"));
        //Kafka消息的序列化方式
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");
        //请求的最长等待时间
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);

        props.put("schema.registry.url", "http://127.0.0.1:8081");
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(USER_SCHEMA);

        //构造Producer对象，注意，该对象是线程安全的，一般来说，一个进程内一个Producer对象即可；
        //如果想提高性能，可以多构造几个对象，但不要太多，最好不要超过5个
        KafkaProducer<String, GenericRecord> producer = new KafkaProducer<String, GenericRecord>(props);

        //构造一个Kafka消息
        String topic = kafkaProperties.getProperty("topic"); //消息所属的Topic，请在控制台申请之后，填写在这里

        try {
            for (int i = 0; i < 100; i++) {
                GenericRecord value = new GenericData.Record(schema);
                value.put("f1", "value" + i);
                ProducerRecord<String, GenericRecord> kafkaMessage = new ProducerRecord<String, GenericRecord>(topic, value);
                //发送消息，并获得一个Future对象
                Future<RecordMetadata> metadataFuture = producer.send(kafkaMessage);
                //同步获得Future对象的结果
                RecordMetadata recordMetadata = metadataFuture.get();
                System.out.println("Produce ok:" + recordMetadata.toString());
            }
        } catch (Exception e) {
            //要考虑重试
            //参考常见报错: https://help.aliyun.com/document_detail/68168.html?spm=a2c4g.11186623.6.567.2OMgCB
            System.out.println("error occurred");
            e.printStackTrace();
        }
    }
}
```


## 消费消息

以java语言消费消息为例，详情参见[kafka-java-demo](https://github.com/AliwareMQ/aliware-kafka-demos/tree/master/kafka-java-demo)。
在kafka-java-demo基础上，我们需要做以下额外配置：

1. 添加avro依赖

```
<dependency>
    <groupId>io.confluent</groupId>
    <artifactId>kafka-avro-serializer</artifactId>
    <version>3.3.0</version>
</dependency>
```

2. 配置schema

```
// 使用KafkaAvroDeserializer进行消息反序列化
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
// schema registry 接入url，即我们在上面步骤中启动的Confluent Schema Registry服务端http接入点
props.put("schema.registry.url", "http://127.0.0.1:8081");
```

3. 接收并解析消息

```
ConsumerRecords<String, GenericRecord> records = consumer.poll(1000);
                //必须在下次poll之前消费完这些数据, 且总耗时不得超过SESSION_TIMEOUT_MS_CONFIG
                //建议开一个单独的线程池来消费消息，然后异步返回结果
                for (ConsumerRecord<String, GenericRecord> record : records) {
                    GenericRecord value = record.value();
                    System.out.println("value = [value.f1 = " + value.get("f1") + ", "
                        + "partition = " + record.partition() + ", " + "offset = " + record.offset());
                }
```

完整消费示例：

```
public class KafkaConsumerDemo {

    public static void main(String args[]) {

        //加载kafka.properties
        Properties kafkaProperties =  JavaKafkaConfigurer.getKafkaProperties();

        Properties props = new Properties();
        //设置接入点，请通过控制台获取对应Topic的接入点
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getProperty("bootstrap.servers"));

        //两次poll之间的最大允许间隔
        //可更加实际拉去数据和客户的版本等设置此值，默认30s
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        //每次poll的最大数量
        //注意该值不要改得太大，如果poll太多数据，而不能在下次poll之前消费完，则会触发一次负载均衡，产生卡顿
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 30);
        //消息的反序列化方式
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.BytesDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        // 添加schema服务的地址，用于获取schema
        props.put("schema.registry.url", "http://127.0.0.1:8081");
        //当前消费实例所属的消费组，请在控制台申请之后填写
        //属于同一个组的消费实例，会负载消费消息
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getProperty("group.id"));
        //构造消息对象，也即生成一个消费实例
        KafkaConsumer<String, GenericRecord> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<String, GenericRecord>(props);
        //设置消费组订阅的Topic，可以订阅多个
        //如果GROUP_ID_CONFIG是一样，则订阅的Topic也建议设置成一样
        List<String> subscribedTopics =  new ArrayList<String>();
        //如果需要订阅多个Topic，则在这里add进去即可
        //每个Topic需要先在控制台进行创建
        subscribedTopics.add(kafkaProperties.getProperty("topic"));
        consumer.subscribe(subscribedTopics);

        //循环消费消息
        while (true){
            try {
                ConsumerRecords<String, GenericRecord> records = consumer.poll(1000);
                //必须在下次poll之前消费完这些数据, 且总耗时不得超过SESSION_TIMEOUT_MS_CONFIG
                //建议开一个单独的线程池来消费消息，然后异步返回结果
                for (ConsumerRecord<String, GenericRecord> record : records) {
                    GenericRecord value = record.value();
                    System.out.println("value = [value.f1 = " + value.get("f1") + ", "
                        + "partition = " + record.partition() + ", " + "offset = " + record.offset());
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (Throwable ignore) {

                }
                //参考常见报错: https://help.aliyun.com/document_detail/68168.html?spm=a2c4g.11186623.6.567.2OMgCB
                e.printStackTrace();
            }
        }
    }
}
```

