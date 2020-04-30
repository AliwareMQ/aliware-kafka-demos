#!/bin/bash
mvn clean package -Dmaven.test.skip=true
cd target/KafkaSpringStreamDemo/KafkaSpringStreamDemo/ && java -cp .:./conf/:./lib/* com.alibaba.cloud.KafkaDemoApplication
