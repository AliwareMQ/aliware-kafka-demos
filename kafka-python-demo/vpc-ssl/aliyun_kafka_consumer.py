#!/usr/bin/env python
# encoding: utf-8

import ssl
from kafka import KafkaConsumer
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
context.load_verify_locations("mix-4096-ca-cert")

consumer = KafkaConsumer(bootstrap_servers=conf['bootstrap_servers'],
                         group_id=conf['consumer_id'],
                         api_version=(0, 10),
                         session_timeout_ms=25000,
                         max_poll_records=100,
                         fetch_max_bytes=1 * 1024 * 1024,
                         security_protocol='SASL_SSL',
                         sasl_mechanism="PLAIN",
                         ssl_context=context,
                         sasl_plain_username=conf['sasl_plain_username'],
                         sasl_plain_password=conf['sasl_plain_password'])

print('consumer start to consuming...')
consumer.subscribe(topics=(conf['topic_name']))
for message in consumer:
    print("Topic:[%s] Partition:[%d] Offset:[%d] Value:[%s]" %
          (message.topic, message.partition, message.offset, message.value))
