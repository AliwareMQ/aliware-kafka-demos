## 下载Mysql Connector
目前[Debezium](https://debezium.io)提供了Mysql Source Connector，我们可以在[这里](https://repo1.maven.org/maven2/io/debezium/debezium-connector-mysql/)下载。注意，下载时需要结合Kafka Connect版本，详情见[这里](https://debezium.io/docs/releases/)。例如，服务端为0.10.2.0版本时，建议下载debezium对应Release 0.5.2版本。
如果用户对Debezium的Mysql Connector实现原理感兴趣，可以参见[官方文档](https://debezium.io/docs/connectors/mysql/)。

## 配置Kakfa Connect

下载完成Mysql Connector之后，解压到特定目录。之后需要在Kafka Connect的配置文件connect-distributed.properties中配置插件安装位置。
```
## 指定插件解压后的路径
plugin.path=/kafka/connect/plugins
```
早期Kafka Connect版本不支持配置plugin.path，需要用户在CLASSPATH中指定插件位置。
```
export CLASSPATH=/path/to/my/connectors/*
```

## 安装Mysql

本示例将会在本地机器通过docker安装Mysql。如果已有Mysql，则可以跳过这一步。
确保已经安装好了docker，执行命令，启动Mysql。
```shell
docker-compose -f docker-compose-mysql.yaml up
```

## 配置Mysql

Debezium的原理是基于mysql binlog技术，所以这里一定需要开启mysql的binlog写入功能，配置binlog模式为row。需要了解更多请参见[Mysql文档](http://dev.mysql.com/doc/refman/5.7/en/replication-options.html)。
```
[mysqld]
log-bin=mysql-bin #添加这一行就ok
binlog-format=ROW #选择row模式
server_id=1 
```

假设我们的Mysql User为debezium，密码为dbz，通过下面语句设置Mysql的User的权限：

```
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'debezium' IDENTIFIED BY 'dbz';
```

如果我们是使用demo中的docker方式安装的Mysql，上面的配置已经默认设置好了，用户可以不需要再重复设置。User为debezium，密码为dbz。

## 启动Kafka Connect
参见![这里](../README.md)

## 启动Mysql Connector

编辑register-mysql.json，更多配置请参见[官方文档](https://debezium.io/docs/connectors/mysql/#connector-properties)

### VPC接入
```
## Kafka接入点，通过控制台获取
## 您在控制台获取的默认接入点
"database.history.kafka.bootstrap.servers" : "kafka:9092",
## 需要提前在控制台创建同名topic，在本例中创建topic：server1
## 所有table的变更数据，会记录在server1.$DATABASE.$TABLE的topic中，如server1.testDB.products
## 因此用户需要提前在控制台中创建所有相关topic
"database.server.name": "server1",
## 记录schema变化信息将记录在这个topic中
## 需要提前在控制台创建
"database.history.kafka.topic": "schema-changes-inventory"
```

### 公网接入
## Kafka接入点，通过控制台获取。存储db中schema变化信息
## 您在控制台获取的SSL接入点
"database.history.kafka.bootstrap.servers" : "kafka:9092",
## 需要提前在控制台创建同名topic，在本例中创建topic：server1
## 所有table的变更数据，会记录在server1.$DATABASE.$TABLE的topic中，如server1.testDB.products
## 因此用户需要提前在控制台中创建所有相关topic
"database.server.name": "server1",
## 记录schema变化信息将记录在这个topic中
## 需要提前在控制台创建
"database.history.kafka.topic": "schema-changes-inventory",
## SSL公网方式访问配置
"database.history.producer.ssl.truststore.location": "kafka.client.truststore.jks",
"database.history.producer.ssl.truststore.password": "KafkaOnsClient",
"database.history.producer.security.protocol": "SASL_SSL",
"database.history.producer.sasl.mechanism": "PLAIN",
"database.history.consumer.ssl.truststore.location": "kafka.client.truststore.jks",
"database.history.consumer.ssl.truststore.password": "KafkaOnsClient",
"database.history.consumer.security.protocol": "SASL_SSL",
"database.history.consumer.sasl.mechanism": "PLAIN",
```

请求Kafka Connect，开启一个Mysql Connector。
```shell
> curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @register-mysql.json
```

## 确认Kafka成功接收到Mysql变更数据

变更监听Mysql Table中的数据，在控制台消息查询页面，确认消息被成功发送到broker中。恭喜，demo至此就跑通了。



