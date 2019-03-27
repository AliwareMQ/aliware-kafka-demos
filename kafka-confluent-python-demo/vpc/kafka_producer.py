from confluent_kafka import Producer
import setting

conf = setting.kafka_setting
"""初始化一个 Producer 对象"""
p = Producer({'bootstrap.servers': conf['bootstrap_servers']})

def delivery_report(err, msg):
    """ Called once for each message produced to indicate delivery result.
        Triggered by poll() or flush(). """
    if err is not None:
        print('Message delivery failed: {}'.format(err))
    else:
        print('Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))

"""异步发送消息"""
p.produce(conf['topic_name'], "Hello".encode('utf-8'), callback=delivery_report)
p.poll(0)

"""在程序结束时，调用flush"""
p.flush()
