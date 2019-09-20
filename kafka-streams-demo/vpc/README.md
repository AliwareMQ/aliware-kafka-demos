# Run Demo

```java
mvn clean package -DskipTests

#copy the target/kafka-streams-demo.jar to somewhere

java -jar kafka-streams-demo.jar <servers>

```

### Topic 资源管理
Kafka Streams 主要依赖于 Kafka 来存储数据及中间状态，会使用到 Kafka 的一些高级特性，例如 compact 类型的 Topic。
生产环境，不允许自动创建 Topic，在跑应用之前，请先按照下面的提示，在云 Kafka 控制台创建 Topic 资源。
以下所有 Topic，分区数请保持一致。

#### source topic
存储源头消息的 Topic，本例中是 streams-plaintext-input，在云 Kafka 控制台创建普通Topic即可。

#### sink  topic
存储最终计算结果的 Topic，本例中是 streams-wordcount-output, 在云 Kafka 控制台，高级配置，选择 Local存储，cleanup.policy 选择 compact。
注意，不是所有结果都需要 compact，请根据业务情况来判断是否需要设置 cleanup.policy 为 compact。

#### internal topic 
存储中间计算结果的 Topic，主要包括：

* 分组操作: groupby，产生的topic名字形式为 \<applicatition-id\>-\<operatorname\>-repartition，选择 Local存储，cleanup.policy 选择 delete;

* 聚合操作: 
 等，产生的topic名字形式为 \<applicatition-id\>-\<operatorname\>-changelog，选择 Local存储，cleanup.policy 选择 compact;


#### Topic 设置汇总

| 类型 | 操作 | 存储属性 | clean.policy |
| --- | --- | --- | --- |
| 源头消息 | stream | 云存储 | 无需设置 |
| 最终结果 | toStream | Local存储 | compact（请根据业务进行调整） |
| 中间分组操作 | groupby | Local存储 | delete |
| 中间聚合操作 | aggregate, reduce, count | Local存储 | compact |



社区文档：
http://kafka.apache.org/23/documentation/streams/developer-guide/manage-topics.html

### 注意事项
如果 AUTO_OFFSET_RESET_CONFIG 设置成 "earliest"，则第一次跑时，会加载所有数据。
在生产环境，如果不想要历史数据，则应用第一次上线时，设置成"latest"

 


