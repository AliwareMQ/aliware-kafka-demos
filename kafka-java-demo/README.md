## KafkaOnsDemo

### Demo跑起来
1. 添加自己的AccessKey，SecretKey到src/main/resources/kafka_client_jaas.conf中
	
2. 在ONS控制台申请Topic，类型选择"Kafka消息", 这里假定为kafka-test(该Topic很有可能已经被别人申请，请修改为自己定义的名字)
3. 执行

	> sh run_demo.sh -Dkafka.ons.TOPIC=kafka-test
	
4. 看到结果如下表示成功(注意，第一次创建topic可能需要点时间，如遇失败，请间隔一定时间多试几次)

	> produce ok:kafka-test-*
5. 遇到问题先参考[Kafka常见报错及排查](https://help.aliyun.com/document_detail/57058.html)
6. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX) 
	
	
### 接入说明

请参考[上一层目录的README](https://github.com/AliwareMQ/aliware-kafka-demos)

#### 接入步骤一：引入依赖
Maven方式：
```
<dependency>
     	<groupId>org.apache.kafka</groupId>
     	<artifactId>kafka-clients</artifactId>
     	<version>0.10.1.0</version>
</dependency>
<dependency>
<groupId>com.aliyun.openservices</groupId>
    	<artifactId>ons-sasl-client</artifactId>
    	<version>0.1</version>
</dependency>
```
#### 接入步骤二：配置AccessKey，SecretKey
Kafka利用AccessKey，SecretKey对通道进行鉴权
新建一个文本文件名为kafka_client_jaas.conf，内容如下(记得把XXX替换为自身的AccessKey，SecretKey)：

```
KafkaClient {
com.aliyun.openservices.ons.sasl.client.OnsLoginModule required
AccessKey="XXX"
SecretKey="XXX";
};
````

放置在某个目录下，这里假定为/home/admin
然后在程序启动的时候，配置
> -Djava.security.auth.login.config=/home/admin/kafka_client_jaas.conf

或者直接在代码中设置（需要保证在Kafka Producer和Consumer启动之前):           
> System.setProperty("java.security.auth.login.config",  “/home/admin/kafka_client_jaas.conf");



#### 接入步骤三：下载根证书
[证书truststore下载地址](http://common-read-files.oss-cn-shanghai.aliyuncs.com/kafka.client.truststore.jks)
将证书放置在某个目录下，这里假定为/home/admin


#### 接入步骤四：配置参数
对于Kafka Producer和Consumer都需要以下几个配置：

|参数|值|
|:--|:--|
|bootstrap.servers|kafka-ons-internet.aliyun.com:8080|
|ssl.truststore.location|/home/admin/kafka.client.truststore.jks|
|ssl.truststore.password|KafkaOnsClient|
|security.protocol|SASL_SSL|
|sasl.mechanism|ONS|


注意
1. /home/admin要改为自己的目录
2. bootstrap.servers请根据region进行选择，region对应列表在[上一层目录的README](https://github.com/AliwareMQ/aliware-kafka-demos)
3. 其它参数遵照[Kafka官网](https://kafka.apache.org/0101/documentation.html)说明即可

### 报错“Failed to send SSL close message”
该错误后面通常还会跟“connection reset by peer”或“broken pipe”。该错误可以忽略，不影响使用。服务端是VIP网络环境，会主动掐掉空闲连接。
你可以通过修改日志级别来避免该错误，以log4j为例，加上下面这行配置：
`log4j.logger.org.apache.kafka.common.network.SslTransportLayer=ERROR`





	


