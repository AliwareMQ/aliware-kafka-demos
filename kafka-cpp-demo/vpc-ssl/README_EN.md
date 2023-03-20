#### Description
The demo aims to show you how to run an application. Refer to official documentation to configure parameters. This helps ensure the robustness of your application and the stability and performance of the client.
For more information, see the [open source website](https://github.com/edenhill/librdkafka), [Introduction to librdkafka](https://github.com/edenhill/librdkafka/blob/master/INTRODUCTION.md#documentation), and [Configuration properties](https://github.com/edenhill/librdkafka/blob/master/CONFIGURATION.md).

#### Install dependencies for CentOS 7
1. Install GNU Compiler Collection (GCC).
```
sudo yum install gcc-c++
```

2. Install Simple Authentication and Security Layer (SASL) and Secure Sockets Layer (SSL) libraries.

```
yum install openssl openssl-devel
yum install cyrus-sasl{,-plain}
```

3. Add a YUM repository.
Go to the /etc/yum.repos.d /directory and create a file named confluent.repo that contains the following content:

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

4. Run the installation command.
```
sudo yum clean all && yum install librdkafka-devel
```



#### Install dependencies for other operating systems
1. Install GCC 4.8.5 or later.
2. Follow the instructions on the [open source website](https://github.com/edenhill/librdkafka) to install dependencies.


#### Procedure
1. For information about endpoints, see [View endpoints](https://help.aliyun.com/document_detail/68342.html?spm=a2c4g.11186623.6.554.X2a7Ga).
2. For information about topics and consumer groups, see [Create resources](https://help.aliyun.com/document_detail/68328.html?spm=a2c4g.11186623.6.549.xvKAt6).
3. You can obtain the username and password on the Instance Details page in the Message Queue for Apache Kafka console.
4. Run the sh comple.sh command to compile the messages.
5. Enter `./kafka_producer <bootstrap_servers> <topic> <username> <password>` and press Enter to send messages.
6. Enter `./kafka_consumer -g <group> -b <bootstrap_servers> -u <username> -p <password> <topic>` and press Enter to consume messages.

#### Test screenshots
![Send and receive messages](https://img.alicdn.com/5476e8b07b923/TB1YQfgScbpK1RjSZFyXXX_qFXa)



