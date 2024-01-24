from confluent_kafka import Consumer, KafkaError

import setting

conf = setting.kafka_setting

c = Consumer({
    'bootstrap.servers': conf['bootstrap_servers'],
    'sasl.mechanisms':'PLAIN',
    'ssl.ca.location':conf['ca_location'],
    # hostname 校验改成空
    'ssl.endpoint.identification.algorithm':'none',
    'security.protocol':'SASL_SSL',
    'sasl.username':conf['sasl_plain_username'],
    'sasl.password':conf['sasl_plain_password'],
    'group.id': conf['group_name'],
    'auto.offset.reset': 'latest',
    'fetch.message.max.bytes':'1024*512'
})

c.subscribe([conf['topic_name']])

while True:
    msg = c.poll(1.0)

    if msg is None:
        continue
    if msg.error():
	    if msg.error().code() == KafkaError._PARTITION_EOF:
		    continue
	    else:
        	print("Consumer error: {}".format(msg.error()))
        	continue

    print('Received message: {}'.format(msg.value().decode('utf-8')))

c.close()
