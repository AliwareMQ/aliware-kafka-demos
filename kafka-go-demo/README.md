### 准备客户端
demo配置的目的仅仅是把应用跑起来作为参考，更多参数和程序健壮性请参考官方文档设置以保证客户端的稳定和性能。
[Kafka 官网 Clients 列表](https://cwiki.apache.org/confluence/display/KAFKA/Clients#Clients-Go(AKAgolang)) 
推荐使用 [开源 sarama GO客户端](https://github.com/Shopify/sarama)进行接入。

### 安装环境
1. 请确保安装了 Go 环境，详情请前往 [golang 官网](https://golang.org "golang 官网") 参考；
2. 执行命令：`git clone https://github.com/AliwareMQ/aliware-kafka-demos.git`
3. 执行命令：`cd aliware-kafka-demos/kafka-go-demo`
4. 执行命令：<code>export GOPATH=\`pwd\`</code>
5. 执行命令安装依赖(请保证联网，需要几分钟，请耐心等待): 
</br>`go get github.com/Shopify/sarama/ ; go get github.com/bsm/sarama-cluster`
6. 执行命令：`go install services`
7. 执行命令：`go install services/producer`
8. 执行命令：`go install services/consumer`
9. 按照本页下面配置说明配置 conf/kafka.json
10. 生产(没报错说明运行成功): `./bin/producer`
11. 消费(没报错说明运行成功)：`./bin/consumer`


### 准备配置

##### beta(废弃)

##### vpc
vpc实例接入demo配置

| Demo 库中配置文件 |配置项| 说明 |
| --- | --- | --- |
| conf/kafka.json | topics | 请修改为控制台上申请的 Topic |
| conf/kafka.json | servers | 请修改为控制台获取的接入点 |
| conf/kafka.json  | consumerGroup | 请修改为控制要申请的 Consumer Group |

##### vpc-ssl
公网ssl实例接入demo配置

| Demo 库中配置文件 |配置项| 说明 |
| --- | --- | --- |
| conf/kafka.json | topics | 请修改为控制台上申请的 Topic |
| conf/kafka.json | servers | 请修改为控制台获取的接入点 |
| conf/kafka.json  | consumerGroup | 请修改为控制要申请的 Consumer Group |
| conf/kafka.json	|username	|请修改为实例详情的用户名|
| conf/kafka.json	|password	|请修改为实例详情的密码|
|conf/kafka.json	|cert_file	|根证书路径，运行 Demo 时无需修改，实际部署时注意相对路径|
