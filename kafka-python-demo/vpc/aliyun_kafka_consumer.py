#!/usr/bin/env python
# encoding: utf-8

import socket
from kafka import KafkaConsumer
from kafka.errors import KafkaError
import setting

conf = setting.kafka_setting

consumer = KafkaConsumer(bootstrap_servers=conf['bootstrap_servers'],
                        group_id=conf['consumer_id'],
                        api_version = (0,10))

print 'consumer start to consuming...'
consumer.subscribe((conf['topic_name'], ))
for message in consumer:
    print message.topic, message.offset, message.key, message.value, message.value, message.partition
