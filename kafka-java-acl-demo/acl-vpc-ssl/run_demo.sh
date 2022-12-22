#!/bin/bash
mvn clean package -Dmaven.test.skip=true
if [ $# -lt 1 ]; then
    echo "Usage: sh $0 <producer|consumer>"
    exit -1
fi
case "$1" in
    "producer")
        echo "run kafka one demo producer"
        cd target/KafkaOnsDemo/KafkaOnsDemo/ && java -cp .:./conf/:./lib/* com.aliyun.openservices.kafka.ons.KafkaProducerDemo
        ;;
    "consumer")
        echo "run kafka one demo producer"
        cd target/KafkaOnsDemo/KafkaOnsDemo/ && java -cp .:./conf/:./lib/* com.aliyun.openservices.kafka.ons.KafkaConsumerDemo
        ;;
    *)
        echo "Unknown runType: $1"
        exit 0
        ;;
esac
