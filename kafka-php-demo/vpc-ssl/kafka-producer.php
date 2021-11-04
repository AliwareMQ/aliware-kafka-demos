<?php

$setting = require __DIR__ . '/setting.php';

$conf = new RdKafka\Conf();
// ---------- 启用 SASL 验证时需要设置 ----------
// SASL 验证机制类型默认选用 PLAIN
$conf->set('sasl.mechanisms', 'PLAIN');
$conf->set('api.version.request', 'true');
// 设置用户名：控制台 配置信息 的用户名
$conf->set('sasl.username', $setting['sasl_plain_username']);
// 设置密码：控制台 配置信息 的密码
$conf->set('sasl.password', $setting['sasl_plain_password']);
$conf->set('security.protocol', 'SASL_SSL');
$conf->set('ssl.ca.location', __DIR__ . '/ca-cert.pem');

// Kafka producer 的 ack 有 3 种机制，分别说明如下：
// -1 或 all：Broker 在 leader 收到数据并同步给所有 ISR 中的 follower 后，才应答给 Producer 继续发送下一条（批）消息。
// 这种配置提供了最高的数据可靠性，只要有一个已同步的副本存活就不会有消息丢失。注意：这种配置不能确保所有的副本读写入该数据才返回，
// 可以配合 Topic 级别参数 min.insync.replicas 使用。
// 0：生产者不等待来自 broker 同步完成的确认，继续发送下一条（批）消息。这种配置生产性能最高，但数据可靠性最低
//（当服务器故障时可能会有数据丢失，如果 leader 已死但是 producer 不知情，则 broker 收不到消息）
// 1： 生产者在 leader 已成功收到的数据并得到确认后再发送下一条（批）消息。这种配置是在生产吞吐和数据可靠性之间的权衡
//（如果leader已死但是尚未复制，则消息可能丢失）
// 用户不显示配置时，默认值为1。用户根据自己的业务情况进行设置
$conf->set('acks', '1');
// 请求发生错误时重试次数，建议将该值设置为大于0，失败重试最大程度保证消息不丢失
$conf->set('retries', '0');
// 发送请求失败时到下一次重试请求之间的时间
$conf->set('retry.backoff.ms', 100);
// producer 网络请求的超时时间。
$conf->set('socket.timeout.ms', 6000);
$conf->set('reconnect.backoff.max.ms', 3000);
// 注册发送消息的回调
$conf->setDrMsgCb(function ($kafka, $message) {
  echo '【Producer】send：message=' . var_export($message, true) . "\n";
});
// 注册发送消息错误的回调
$conf->setErrorCb(function ($kafka, $err, $reason) {
  echo "【Producer】send error：err=$err reason=$reason \n";
});

$rk = new RdKafka\Producer($conf);
# if want to debug, set log level to LOG_DEBUG
$rk->setLogLevel(LOG_INFO);
// 设置入口服务，请通过控制台获取对应的服务地址。
$rk->addBrokers($setting['bootstrap_servers']);
$topic = $rk->newTopic($setting['topic_name']);
// RD_KAFKA_PARTITION_UA 让 kafka 自由选择分区
$a = $topic->produce(RD_KAFKA_PARTITION_UA, 0, "Message hello kafka");
$rk->poll(0);
while ($rk->getOutQLen() > 0) {
    $rk->poll(50);
}
echo "send success" . PHP_EOL;

