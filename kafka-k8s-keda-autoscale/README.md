## 简介

本Demo使用keda来对Kafka消费者个数进行水平扩缩容，从而把堆积量控制在一个阈值内。

### 前置条件
Kafka 服务端版本需要2.2.0

### 部署 keda
建议参照[官网](https://keda.sh/docs/2.0/deploy/#yaml)部署, 使用helm，几个命令即可完成。

```$xslt
# Add Helm repo
helm repo add kedacore https://kedacore.github.io/charts
# Update Helm repo
helm repo update
# Install keda Helm chart Helm 3
kubectl create namespace keda
helm install keda kedacore/keda --namespace keda
```
安装完成之后
```$xslt
kubectl get pod -A|grep keda
```
应该可以看到名字类似"keda-operator-XXX"和"keda-operator-metrics-apiserver-XXX"两个pod处于running状态。
说明部署成功。

### 修改配置
请使用自己的实例配置，替代各个yaml文件中的内容。

### 部署 目标组件 kafka-consumer
所谓目标组件，就是当条件触发时，被扩容/缩容的组合。
```
kubectl apply -f kafka-consumer.yaml

kubectl get pod|grep alibaba-kafka-consumer
```

### 部署 触发器
所以触发器，就是当条件达成时，对目标组件进行扩容或者缩容。
在本例中，当堆积超过100时，对目标组件进行扩容。

```$xslt
kubectl apply -f scale-kafka.yaml
```

### 模拟数据
```$xslt
kubectl apply -f kafka-producer.yaml
kubectl get pod|grep alibaba-kafka-producer
```
在本例中，consumer和producer的速度是配置成大致相等的。
可以通过调整kafka-producer的replicas个数来观察kafka-consumer的个数是否会自动发生变化。