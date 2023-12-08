import os

bootstrap_servers = os.environ.get('ALI_KAFKA_BOOTSTRAP_SERVERS', 'kafka-1:9092,kafka-2:9092.kafka-3:9092').split(',')
topic_name        = os.environ.get('ALI_KAFKA_TOPIC_NAME', 'YOUR_TOPIC_NAME')
consumer_id       = os.environ.get('ALI_KAFKA_CONSUMER_ID', 'YOUR_GROUP_ID')

kafka_setting = {
    'bootstrap_servers': bootstrap_servers,
    'topic_name': topic_name,
    'group_name': consumer_id,
}
