## Run Demo
该Demo，需要Kafka 服务端升级到2.x版本。
早期版本，请参考本仓库的分支:spring.cloud.Camden。
demo的目的仅仅是把应用跑起来作为参考，更多参数请参考文档设置以保证客户端的稳定和性能。

1. 安装软件：确保安装了 JDK 8+ 和 Maven 3.2.5+
2. 编写配置：修改application.properties XXX 替换为实际值
3. 发收消息：sh run_demo.sh 

## 配置参数
1. 修改application.properties中的
kafka.bootstrap-servers=XXX
kafka.consumer.group=XXX
kafka.output.topic.name=XXX
kafka.input.topic.name=XXX
kafka.ssl.truststore.location=/XXX/kafka.client.truststore.jks

2. 修改kafka_client_jaas.conf中的username和password，可从实例详情获取。

3. 可根据需求修改相应的Output和Input

## 文档
参考 https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/

	


