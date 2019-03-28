#!/bin/bash 

#编译Producer
gcc -lrdkafka ./kafka_producer.c -o kafka_producer

#编译Consumer
gcc -lrdkafka ./kafka_consumer.c -o kafka_consumer
