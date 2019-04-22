<?php
$setting = require __DIR__ . '/setting.php';
$conf = new RdKafka\Conf();
$conf->set('api.version.request', 'true');

$conf->set('group.id', $setting['consumer_id']);

$conf->set('metadata.broker.list', $setting['bootstrap_servers']);

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
            var_dump($message);
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
