#!/bin/bash
mvn clean package -Dmaven.test.skip=true

echo "Run springboot kafka [vpc-ssl] demo"
cd target && java -jar vpc-ssl-0.0.1-SNAPSHOT.jar


