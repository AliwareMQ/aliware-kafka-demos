#!/bin/bash
mvn clean package -Dmaven.test.skip=true
if [ $# -lt 1 ]; then
    echo "Usage: sh $0 <producer|consumer>"
    exit -1
fi
case "$1" in
    "producer")
        echo "run kafka one demo producer"
        cd target && java  -jar -Dloader.main=com.aliyun.openservices.kafka.ons.producer.KafkaProducerDemo -Djava.security.auth.login.config=XXX/kafka_client_jaas.conf  kafka-vpc-ssl-springboot-demo-1.0-SNAPSHOT.jar
        ;;
    "consumer")
        echo "run kafka one demo producer"
        cd target && java  -jar -Dloader.main=com.aliyun.openservices.kafka.ons.consumer.KafkaConsumerDemo -Djava.security.auth.login.config=XXX/kafka_client_jaas.conf  kafka-vpc-ssl-springboot-demo-1.0-SNAPSHOT.jar
        ;;
    *)
        echo "Unknown runType: $1"
        exit 0
        ;;
esac
