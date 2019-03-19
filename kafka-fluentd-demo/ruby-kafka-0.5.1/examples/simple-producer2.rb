# Reads lines from STDIN, writing them to Kafka.
#
# You need to define the environment variable KAFKA_BROKERS for this
# to work, e.g.
#
#     export KAFKA_BROKERS=localhost:9092
#

$LOAD_PATH.unshift(File.expand_path("../../lib", __FILE__))

require "kafka"

logger = Logger.new($stderr)
brokers = "localhost:9093"

# Make sure to create this topic in your Kafka cluster or configure the
# cluster to auto-create topics.
topic = "FluentdTopicOutput"
#ssl_ca_cert = "/work/kafka-config/sasl/kafka-ons-internet.aliyun.com.perm"
ssl_ca_certs_from_system = false
#sasl_plain_username = "wqtest3"
#sasl_plain_password = "wiseking"
username = "wqtest3"
password = "wiseking"

kafka = Kafka.new(
  seed_brokers: brokers,
  client_id: "simple-producer",
  logger: logger,
  ssl_ca_cert:  File.read('/work/kafka-config/sasl/kafka.cert'),
  sasl_plain_username: "wqtest3",
  sasl_plain_password: "wiseking",
)

producer = kafka.producer

begin
  $stdin.each_with_index do |line, index|
    producer.produce(line, topic: topic)

    # Send messages for every 10 lines.
    producer.deliver_messages if index % 10 == 0
  end
ensure
  # Make sure to send any remaining messages.
  producer.deliver_messages

  producer.shutdown
end
