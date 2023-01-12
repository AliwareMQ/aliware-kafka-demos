#!/bin/bash
mvn clean package -Dmaven.test.skip=true

echo "Run springboot kafka [vpc-9094] demo"
cd target && java -jar vpc-9094-0.0.1-SNAPSHOT.jar


