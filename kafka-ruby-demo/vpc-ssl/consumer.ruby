# frozen_string_literal: true

$LOAD_PATH.unshift(File.expand_path("../../lib", __FILE__))

require "kafka"

logger = Logger.new(STDOUT)
#logger.level = Logger::DEBUG
logger.level = Logger::INFO

brokers = "xxx:xx,xxx:xx"
topic = "xxx"
username = "xxx"
password = "xxx"
consumerGroup = "xxx"

kafka = Kafka.new(
        seed_brokers: brokers,
        client_id: "sasl-consumer",
        socket_timeout: 20,
        logger: logger,
        # put "./cert.pem" to anywhere this can read
        ssl_ca_cert: File.read('./cert.pem'),
        sasl_plain_username: username,
        sasl_plain_password: password,
        )

consumer = kafka.consumer(group_id: consumerGroup)
consumer.subscribe(topic, start_from_beginning: false)

trap("TERM") { consumer.stop }
trap("INT") { consumer.stop }

begin
    consumer.each_message(max_bytes: 64 * 1024) do |message|
    logger.info("Get message: #{message.value}")
    end
rescue Kafka::ProcessingError => e
    warn "Got error: #{e.cause}"
    consumer.pause(e.topic, e.partition, timeout: 20)

    retry
end
