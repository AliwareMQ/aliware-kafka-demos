from confluent_kafka import Producer
import setting

conf = setting.kafka_setting


def delivery_report(err, msg):
    """ Called once for each message produced to indicate delivery result.
        Triggered by poll() or flush(). """
    if err is not None:
        print('Message delivery failed: {}'.format(err))
    else:
        print('Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))

p = Producer({'bootstrap.servers': conf['bootstrap_servers']})
p.produce(conf['topic_name'], "Hello".encode('utf-8'), callback=delivery_report)
p.poll(0)
p.flush()
