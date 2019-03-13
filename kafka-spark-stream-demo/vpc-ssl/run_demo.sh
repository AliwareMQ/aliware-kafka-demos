#!/bin/bash
if [ $# -lt 1 ]; then
    echo "Usage: sh $0 <sparkConsumer|producer>"
    exit -1
fi
case "$1" in
    "sparkConsumer")
        echo "run kafka one demo sparkConsumer"
        cd target/AliKafkaSparkDemo/AliKafkaSparkDemo/ && java -cp .:./conf/:./lib/* com.aliyun.openservices.alikafka.SparkKafkaConsumerDemo
        ;;
    "producer")
        echo "run kafka one demo producer"
        cd target/AliKafkaSparkDemo/AliKafkaSparkDemo/ && java -cp .:./conf/:./lib/* com.aliyun.openservices.alikafka.KafkaProducerDemo
        ;;
    *)
        echo "Unknown runType: $1"
        exit 0
        ;;
esac
