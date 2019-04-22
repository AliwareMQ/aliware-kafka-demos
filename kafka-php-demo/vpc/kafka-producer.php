<?php

$setting = require __DIR__ . '/setting.php';

$conf = new RdKafka\Conf();
$conf->set('api.version.request', 'true');
$conf->set('message.send.max.retries', 5);
$rk = new RdKafka\Producer($conf);
## If want to debug, set log level to LOG_DEBUG
$rk->setLogLevel(LOG_INFO);
$rk->addBrokers($setting['bootstrap_servers']);
$topic = $rk->newTopic($setting['topic_name']);
$a = $topic->produce(RD_KAFKA_PARTITION_UA, 0, "Message hello kafka");
$rk->poll(0);
while ($rk->getOutQLen() > 0) {
    $rk->poll(50);
}
echo "send succ" . PHP_EOL;

