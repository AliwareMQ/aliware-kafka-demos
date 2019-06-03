## 下载SQL Server Connector
目前[Debezium](https://debezium.io)提供了SQL Server Source Connector，我们可以在[这里](https://repo1.maven.org/maven2/io/debezium/debezium-connector-sqlserver/)下载。注意，下载时需要结合Kafka Connect版本，详情见[这里](https://debezium.io/docs/releases/)。
如果用户对Debezium的SQL Server Connector实现原理感兴趣，可以参见[官方文档](https://debezium.io/docs/connectors/sqlserver/)。

## 配置Kakfa Connect

下载完成SQL Server Connector之后，解压到特定目录。之后需要在Kafka Connect的配置文件connect-distributed.properties中配置插件安装位置。
```
## 指定插件解压后的路径
plugin.path=/kafka/connect/plugins
```
早期Kafka Connect版本不支持配置plugin.path，需要用户在CLASSPATH中指定插件位置。
```
export CLASSPATH=/path/to/my/connectors/*
```

## 安装SQL Server

由于在[SQL Server 2016 SP1](https://blogs.msdn.microsoft.com/sqlreleaseservices/sql-server-2016-service-pack-1-sp1-released/) 之后开始支持[CDC](https://docs.microsoft.com/en-us/sql/relational-databases/track-changes/about-change-data-capture-sql-server?view=sql-server-2017)，因此需要SQL Server版本不能低于该版本。

本示例将会在本地机器通过docker安装SQL Server。如果已有SQL Server，则可以跳过这一步。
确保已经安装好了docker，执行命令，启动SQL Server。
```shell
docker-compose -f docker-compose-sqlserver.yaml up
```

## 配置SQL Server

SQL Server需要做如下配置：
### 开启CDC配置
```
## Enable Database for CDC template
USE testDB
GO
EXEC sys.sp_cdc_enable_db
GO
```
### 指定table开启CDC配置
```
## Enable a Table Specifying Filegroup Option Template
USE testDB
GO

EXEC sys.sp_cdc_enable_table
@source_schema = N'dbo',
@source_name   = N'MyTable',
@role_name     = N'MyRole',
@filegroup_name = N'MyDB_CT',
@supports_net_changes = 1
GO
```
### 确认用户有权限访问CDC table
```
EXEC sys.sp_cdc_help_change_data_capture
GO
```
如果返回的结果为空，需要确认用户是否有权限访问该表。

### 确认SQL Server Agent开启
```
EXEC master.dbo.xp_servicecontrol N'QUERYSTATE',N'SQLSERVERAGENT'
```
如果返回结果为RUNNING. 则说明已经开启。

设置并初始化SQL Server中的测试数据。
```shell
cat inventory.sql | docker exec -i tutorial_sqlserver_1 bash -c '/opt/mssql-tools/bin/sqlcmd -U sa -P $SA_PASSWORD'
```
成功后，可以看到创建了新的database、tables以及初始数据。此时可以进入下一步。

## 启动Kafka Connect
参见[这里](../README.md)

## 启动SQL Server Connector

### 编辑register-sqlserver.json
更多配置请参见[官方文档](https://debezium.io/docs/connectors/sqlserver/#connector-properties)

#### VPC接入
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

#### 公网接入
```
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
```

### 创建相关Topic
配置好register-sqlserver.json，需根据配置在控制台中创建相应topic。
例如，如果是按照本例子中的方式安装的Mysql，可以看到SQL Server已经提前创建好了db name：testDB，下面有四张表：customers, orders, products以及products_on_hand。
根据以上register-sqlserver.json的配置，我们需要在控制台创建topic：server1, server1.testDB.customers, server1.testDB.orders, server1.testDB.products, server1.testDB.products_on_hand。此外，在register-sqlserver.json中，配置了将schema变化信息记录在schema-changes-testDB，因此还需要在控制台创建topic：schema-changes-inventory。

### 发送请求，启动SQL Server Connector
请求Kafka Connect，开启一个SQL Server Connector。
```shell
> curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @register-sqlserver.json
```

## 确认Kafka成功接收到SQL Server变更数据

在控制台消息查询页面，确认消息被成功发送到broker中。恭喜，demo至此就跑通了。



