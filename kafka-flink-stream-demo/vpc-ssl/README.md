# Run Demo

本Demo演示了AliKafka Topic上的消息作为Flink的流数据源，被Flink进行消费和处理。

## 运行步骤

### 1. 安装软件：确保安装了 JDK 8+ 和 Maven 3.2.5+
### 2. 修改配置文件：`src/main/resources/kafka.properties`
```
在控制台上获取当前Kafka实例的"SSL接入点"地址，将它设置给属性 bootstrap.servers
在控制台上该Kafka实例下创建测试topic, 如: kafka-spark-test，并将它设置给属性 topic
在控制台上创建消费组, 如: spark-executor-TEST, 并将它去除前缀"spark-executor-"后的取值设置给属性 group.id
```
### 3. 修改SASL配置文件：`src/main/resources/kafka_client_jaas.conf`
```
在控制台实例配置信息下获取用户名和密码，设置到username和password属性值上
```
### 4. 编译及运行
* 进行maven编译
```
  mvn clean package -Dmaven.test.skip=true
```

* 运行下面的命令，启动Spark服务，并作为Kafka消费端
```
sh run_demo.sh flinkConsumer
确保服务启动没有报错退出，当topic中有新消息时，日志中会打印相关消费的消息
```

* 在新的终端中，运行下面的命令，向指定的Kafka topic内发送消息
```
sh run_demo.sh producer
```


