### Aliware Kafka Demos
这里提供各种接入 Aliware Kafka 的demo，方便开发者快速上手
#### 接入说明
1. Kafka Client版本需要0.10及以上
2. 使用之前需要先申请Topic(类型选择**Kafka消息**)与Consumer ID，详情请参考[申请MQ资源](https://help.aliyun.com/document_detail/29536.html?spm=5176.doc29546.2.2.gWIToO)
3. 如果没有申请Topic与CID，则会直接导致鉴权失败
4. 欢迎加钉钉群咨询，用钉钉扫描[群二维码](http://img3.tbcdn.cn/5476e8b07b923/TB1HEQgQpXXXXbdXVXXXXXXXXXX)
5. 各类客户端的详细接入请参考具体仓库目录，如kafka-java-demo
6. 附各region域名列表


| Region | 域名 |
| --- | --- |
| 公网 | kafka-ons-internet.aliyun.com:8080 |
| 华东1 | 敬请期待 |

#### 接入优势
* 无缝迁移：业务系统基于现有的 Kafka 代码与生态进行开发，无需任何改造，即可无缝迁移到阿里云消息队列提供的 Kafka 消息服务。

* 安全防护：Aliware MQ 提供的 Kafka 消息服务利用 SASL 机制对用户身份进行认证，并利用 SSL 对通道进行加密，提供更加安全的消息服务。

* 高可靠/高可用：消息持久化落盘到消息队列，支持多副本以及主备自动切换，可靠性达99.99999999%，服务可用性高达99.999%。

* 高性能：支持万级 Topic，以及海量的消息堆积的情况下，也始终保持超高的性能。

* 统一运维：提供一整套包括资源申请、资源授权、消息堆积查询等运维服务，提升用户的产品体验。

* 消息互通：您可以通过 Kafka Client 发送消息，MQ Client 订阅消息。反之亦然，真正做到 Aliware MQ 与 Kafka 消息完全互通。

* 免费试用：Kafka 企业级消息服务于 2017.03.15 开放公测，公测期间不收取API调用费，仅仅收取topic资源占用费，每个topic每天2元。

	


