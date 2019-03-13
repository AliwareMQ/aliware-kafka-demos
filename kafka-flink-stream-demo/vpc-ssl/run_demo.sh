#!/bin/bash
if [ $# -lt 1 ]; then
    echo "Usage: sh $0 <flinkConsumer|producer>"
    exit -1
fi
case "$1" in
    "flinkConsumer")
        echo "run kafka one demo flinkConsumer"
        cd target/AliKafkaFlinkDemo/AliKafkaFlinkDemo/ && java -cp .:./conf/:./lib/* com.aliyun.openservices.alikafka.FlinkKafkaConsumerDemo
        ;;
    "producer")
        echo "run kafka one demo producer"
        cd target/AliKafkaFlinkDemo/AliKafkaFlinkDemo/ && java -cp .:./conf/:./lib/* com.aliyun.openservices.alikafka.KafkaProducerDemo
        ;;
    *)
        echo "Unknown runType: $1"
        exit 0
        ;;
esac
