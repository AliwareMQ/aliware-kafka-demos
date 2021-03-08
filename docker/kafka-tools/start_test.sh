#!/usr/bin/env bash
base_dir="/home/admin"
cd $base_dir

source /home/admin/kafka-tools/parse_env.sh

cd /home/admin


if [ "X$produce_enable" == "Xtrue" ]; then
 su root -m -c " java -jar KafkaChecker.jar send -s $kafka_server -t $kafka_topic  -c 100000000000 --tps 100 -b 1 &"
fi

if [ "X$consume_enable" == "Xtrue" ]; then
 su root -m -c " java -jar KafkaChecker.jar consume -s $kafka_server -t $kafka_topic -g $kafka_group --commit --keepConsuming --printAll true --tps 120 &"
fi

tail -f /etc/hosts > /dev/null
