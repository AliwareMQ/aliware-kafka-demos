from confluent_kafka import Consumer, KafkaError

import setting

conf = setting.kafka_setting



c = Consumer({
    'bootstrap.servers': conf['bootstrap_servers'],
    'group.id': conf['group_name'],
    'auto.offset.reset': 'latest'
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
