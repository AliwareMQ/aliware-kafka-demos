#!/usr/bin/env python
# encoding: utf-8

import ssl
import socket
from kafka import KafkaConsumer
from kafka.errors import KafkaError
import setting

conf = setting.kafka_setting


context = ssl.create_default_context()
context = ssl.SSLContext(ssl.PROTOCOL_SSLv23)
## The new python(2.7.8+) may cannot ignore the hostname check,
## you could set to ssl.CERT_NONE to walk around the problem,
## or you can change the client to confluent-python-demo

#context.verify_mode = ssl.CERT_NONE
context.verify_mode = ssl.CERT_REQUIRED

context.check_hostname = False
context.load_verify_locations("ca-cert")

consumer = KafkaConsumer(bootstrap_servers=conf['bootstrap_servers'],
                        group_id=conf['consumer_id'],
                        sasl_mechanism="PLAIN",
                        ssl_context=context,
                        security_protocol='SASL_SSL',
                        api_version = (0,10),
                        sasl_plain_username=conf['sasl_plain_username'],
                        sasl_plain_password=conf['sasl_plain_password'])

print 'consumer start to consuming...'
consumer.subscribe((conf['topic_name'], ))
for message in consumer:
    print message.topic, message.offset, message.key, message.value, message.value, message.partition
