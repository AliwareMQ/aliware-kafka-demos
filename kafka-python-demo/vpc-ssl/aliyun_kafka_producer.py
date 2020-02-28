#!/usr/bin/env python
# encoding: utf-8

import ssl
import socket
from kafka import KafkaProducer
from kafka.errors import KafkaError
import setting

conf = setting.kafka_setting

print conf

context = ssl.create_default_context()
context = ssl.SSLContext(ssl.PROTOCOL_SSLv23)
context.verify_mode = ssl.CERT_NONE
context.check_hostname = False
context.load_verify_locations("ca-cert")

producer = KafkaProducer(bootstrap_servers=conf['bootstrap_servers'],
                        sasl_mechanism="PLAIN",
                        ssl_context=context,
                        security_protocol='SASL_SSL',
                        api_version = (0,10),
                        retries=5,
                        sasl_plain_username=conf['sasl_plain_username'],
                        sasl_plain_password=conf['sasl_plain_password'])

partitions = producer.partitions_for(conf['topic_name'])
print 'Topic下分区: %s' % partitions

try:
    future = producer.send(conf['topic_name'], 'hello aliyun-kafka!')
    future.get()
    print 'send message succeed.'
except KafkaError, e:
    print 'send message failed.'
    print e
