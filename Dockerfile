# 
# @refernence: 
#   https://www.alibabacloud.com/help/zh/message-queue-for-apache-kafka/latest/sdk-for-python-send-and-consume-messages-by-using-an-ssl-endpoint-with-plain-authentication
#   https://github.com/AliwareMQ/aliware-kafka-demos
# 
FROM ubuntu:22.04

ENV ALI_KAFKA_BOOTSTRAP_SERVERS="kafka-1:9092,kafka-2:9092.kafka-3:9092"
ENV ALI_KAFKA_TOPIC_NAME="YOUR_TOPIC_NAME"
ENV ALI_KAFKA_CONSUMER_ID="YOUR_GROUP_ID"
ENV ALI_KAFKA_DEMOS_REPO="https://github.com/AliwareMQ/aliware-kafka-demos"

RUN apt-get update \
    && apt-get -yq install \
    git \
    wget \
    python3 \
    python3-pip \
    && pip install confluent-kafka==1.9.2 \
    && wget https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20220826/ytsw/only-4096-ca-cert 

ADD . /aliware-kafka-demos/
WORKDIR /aliware-kafka-demos/
CMD ["/bin/sleep", "infinity"]
