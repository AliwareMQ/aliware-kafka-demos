$LOAD_PATH.unshift(File.expand_path("../../lib", __FILE__))

require "kafka"

logger = Logger.new(STDOUT)
brokers = ENV.fetch("KAFKA_BROKERS", "localhost:9093").split(",")

# Make sure to create this topic in your Kafka cluster or configure the
# cluster to auto-create topics.
topic = "FluentdInput"

kafka = Kafka.new(
  seed_brokers: brokers,
  client_id: "test",
  socket_timeout: 20,
  logger: logger,
  ssl_ca_cert: File.read('/work/kafka-config/sasl/kafka.cert'), 
  sasl_plain_username: "wqtest3",
  sasl_plain_password:  "wiseking",
)

consumer = kafka.consumer(group_id: "wq")
consumer.subscribe(topic)

trap("TERM") { consumer.stop }
trap("INT") { consumer.stop }
puts "wiseking"

begin
  consumer.each_message do |message|
puts message.offset, message.key, message.value
  end
rescue Kafka::ProcessingError => e
   puts "Got #{e.cause}"
  consumer.pause(e.topic, e.partition, timeout: 20)

  retry
end
