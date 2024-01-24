from confluent_kafka import Producer
import setting

conf = setting.kafka_setting

p = Producer({'bootstrap.servers':conf['bootstrap_servers'],
	'sasl.mechanisms':'PLAIN',
	'ssl.ca.location':conf['ca_location'],
	'security.protocol':'SASL_SSL',
    # hostname 校验改成空
	'ssl.endpoint.identification.algorithm':'none',
	'sasl.username':conf['sasl_plain_username'],
	'sasl.password':conf['sasl_plain_password']})


def delivery_report(err, msg):
    if err is not None:
        print('Message delivery failed: {}'.format(err))
    else:
        print('Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))

p.produce(conf['topic_name'], "Hello".encode('utf-8'), callback=delivery_report)
p.poll(0)

p.flush()