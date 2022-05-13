<?php
$setting = require __DIR__ . '/setting.php';
$conf = new RdKafka\Conf();
// SASL 验证机制类型默认选用 PLAIN
$conf->set('sasl.mechanisms', 'PLAIN');
$conf->set('api.version.request', 'true');
// 设置用户名：控制台 配置信息 的用户名
$conf->set('sasl.username', $setting['sasl_plain_username']);
// 设置密码：控制台 配置信息 的密码
$conf->set('sasl.password', $setting['sasl_plain_password']);
$conf->set('security.protocol', 'SASL_SSL');
$conf->set('ssl.ca.location', __DIR__ . '/mix-4096-ca-cert');

$conf->set('group.id', $setting['consumer_id']);
// 设置入口服务，请通过控制台获取对应的服务地址。
$conf->set('metadata.broker.list', $setting['bootstrap_servers']);

// 使用 Kafka 消费分组机制时，消费者超时时间。当 Broker 在该时间内没有收到消费者的心跳时，
// 认为该消费者故障失败，Broker 发起重新 Rebalance 过程。
$conf->set('session.timeout.ms', 10000);
// 客户端请求超时时间，如果超过这个时间没有收到应答，则请求超时失败
$conf->set('request.timeout.ms', 305000);
// 设置客户端内部重试间隔。
$conf->set('reconnect.backoff.max.ms', 3000);

$topicConf = new RdKafka\TopicConf();

$conf->setDefaultTopicConf($topicConf);

$consumer = new RdKafka\KafkaConsumer($conf);

$consumer->subscribe([$setting['topic_name']]);

echo "Waiting for partition assignment... (make take some time when\n";
echo "quickly re-joining the group after leaving it.)\n";

while (true) {
    $message = $consumer->consume(30 * 1000);
    switch ($message->err) {
        case RD_KAFKA_RESP_ERR_NO_ERROR:
            echo "[Consumer] receives the message：" . var_export($message, true) . "\n";
            break;
        case RD_KAFKA_RESP_ERR__PARTITION_EOF:
            echo "No more messages; will wait for more\n";
            break;
        case RD_KAFKA_RESP_ERR__TIMED_OUT:
            echo "Timed out\n";
            break;
        default:
            throw new \Exception($message->errstr(), $message->err);
            break;
    }
}

?>
