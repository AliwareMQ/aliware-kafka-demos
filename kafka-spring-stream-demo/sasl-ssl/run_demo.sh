#!/bin/bash
mvn clean package -Dmaven.test.skip=true
cd target/KafkaOnsDemo/KafkaOnsDemo/ && java -cp .:./conf/:./lib/* com.alibaba.cloud.KafkaDemoApplication
