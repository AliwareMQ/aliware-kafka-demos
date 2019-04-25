#### 接入说明
Demo 的目的仅仅是把应用跑起来作为参考，更多参数和程序健壮性请参考官方文档设置以保证客户端的稳定和性能。
相关资料请参考 [开源源码](https://github.com/edenhill/librdkafka) 和 [开源文档](https://github.com/edenhill/librdkafka/blob/master/INTRODUCTION.md#documentation) 和 [配置文档](https://github.com/edenhill/librdkafka/blob/master/CONFIGURATION.md).

#### CentOS 7 安装依赖
1.安装 GCC
```
sudo yum install gcc-c++
```

2.安装 SASL 和 SSL 库

```
yum install openssl openssl-devel
yum install cyrus-sasl{,-plain}
```

3.添加 yum 仓库
进入 /etc/yum.repos.d/ 目录，然后创建一个文件名为 confluent.repo，文件内包含以下内容：

```
[Confluent.dist]
name=Confluent repository (dist)
baseurl=https://packages.confluent.io/rpm/5.1/7
gpgcheck=1
gpgkey=https://packages.confluent.io/rpm/5.1/archive.key
enabled=1

[Confluent]
name=Confluent repository
baseurl=https://packages.confluent.io/rpm/5.1
gpgcheck=1
gpgkey=https://packages.confluent.io/rpm/5.1/archive.key
enabled=1
```

4.执行安装命令
```
sudo yum clean all && yum install librdkafka-devel
```



#### 其它系统安装依赖
1.安装 gcc 4.8.5 及以上版本；
2.按照[开源官网](https://github.com/edenhill/librdkafka)的 Instructions 进行尝试；


#### 接入步骤
1. bootstrap_servers请参考文档[获取接入点](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga) 
2. Topic与ConsumerGroup请参考文档[创建资源](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6)
3. username和password在控制台的查看实例详情页里获取
4. 编译：sh comple.sh
5. 发送消息（输入内容然后Enter就是触发发送）：`./kafka_producer <bootstrap_servers> <topic> <username> <password>`
6. 消费消息：`./kafka_consumer -g <group> -b <bootstrap_servers> -u <username> -p <password> <topic>`

#### 测试截图
![收发消息](https://img.alicdn.com/5476e8b07b923/TB1YQfgScbpK1RjSZFyXXX_qFXa)



