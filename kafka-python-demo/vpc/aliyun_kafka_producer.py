#!/usr/bin/env python
# encoding: utf-8

import socket
from kafka import KafkaProducer
from kafka.errors import KafkaError
import setting

conf = setting.kafka_setting

print conf


producer = KafkaProducer(bootstrap_servers=conf['bootstrap_servers'],
                        api_version = (0,10),
                        retries=5)

partitions = producer.partitions_for(conf['topic_name'])
print 'Topic下分区: %s' % partitions

try:
    future = producer.send(conf['topic_name'], 'hello aliyun-kafka!')
    future.get()
    print 'send message succeed.'
except KafkaError, e:
    print 'send message failed.'
    print e
