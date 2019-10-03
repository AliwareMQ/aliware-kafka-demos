#!/bin/bash
mvn clean package -Dmaven.test.skip=true
cp target/kafka-vpc-demo.jar .