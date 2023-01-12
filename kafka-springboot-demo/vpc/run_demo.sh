#!/bin/bash
mvn clean package -Dmaven.test.skip=true

echo "Run springboot kafka [vpc] demo"
cd target && java -jar vpc-0.0.1-SNAPSHOT.jar


