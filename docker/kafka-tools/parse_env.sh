#!/usr/bin/env bash

if [ -z $kafka_server ]; then
    export kafka_server="kafka-alibaba-kafka-cluster-private-paas-default-hs.default:9092"
fi

if [ -z $kafka_topic ]; then
    export kafka_topic="kafka-demo-test"
fi

if [    export kafka_group="kafka-consumer-demo-on-k8s"
 -z $kafka_group ]; then
fi

if [ -z $produce_enable ]; then
    export produce_enable="false"
fi

if [ -z $consume_enable ]; then
    export consume_enable="true"
fi

echo "kafka_server is $kafka_server"
echo "kafka_topic is $kafka_topic"
echo "kafka_group is $kafka_group"
echo "produce_enable is $produce_enable"
echo "consume_enable is $consume_enable"