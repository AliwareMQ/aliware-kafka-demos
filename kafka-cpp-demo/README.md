### 接入说明

待完成。
Node.js客户端实际上是包装[C++客户端](https://github.com/edenhill/librdkafka)实现的，可以参考其配置方式进行调试。


### 配置说明

| demo中配置文件 | 配置项 | 说明 |
| --- | --- | --- |
|  | topic | 请改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的Topic(类别Kafka消息) |
|  | bootstrap.servers | 请根据[region列表](https://github.com/AliwareMQ/aliware-kafka-demos)进行选取 |
|   | sasl.username | 请修改为阿里云账号的AccessKey |
|   | sasl.password | 请修改为阿里云账号的SecretKey的后10位 |
|   | group.id | 请修改为[AliwareMQ控制台](https://help.aliyun.com/document_detail/29536.html)上申请的ConsumerID |
|  | ssl.ca.location | 根证书路径，运行Demo时无需修改，实际部署时注意路径 |





