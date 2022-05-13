# frozen_string_literal: true

$LOAD_PATH.unshift(File.expand_path("../../lib", __FILE__))

require "kafka"

logger = Logger.new($stdout)
#logger.level = Logger::DEBUG
logger.level = Logger::INFO

brokers = "xxx:xx,xxx:xx"
topic = "xxx"
username = "xxx"
password = "xxx"

kafka = Kafka.new(
	seed_brokers: brokers,
	client_id: "sasl-producer",
	logger: logger,
	# put "./cert.pem" to anywhere this can read
    ssl_ca_cert: File.read('./mix-4096-ca-cert'),
    sasl_plain_username: username,
    sasl_plain_password: password,
	)

producer = kafka.producer

begin
    $stdin.each_with_index do |line, index|

    producer.produce(line, topic: topic)

    producer.deliver_messages
end

ensure
# Make sure to send any remaining messages.
    producer.deliver_messages

    producer.shutdown
end