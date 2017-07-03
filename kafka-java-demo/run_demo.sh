#!/bin/bash
mvn clean package -Dmaven.test.skip=true
case $1 in 
    *)
        echo "run kafka one demo"
        cd target/KafkaOnsDemo/KafkaOnsDemo/ && java -cp .:./conf/:./lib/* "$@" com.aliyun.openservices.kafka.ons.KafkaProducerDemo 
        ;;
esac
