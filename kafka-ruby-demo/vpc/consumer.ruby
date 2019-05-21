# frozen_string_literal: true

$LOAD_PATH.unshift(File.expand_path("../../lib", __FILE__))

require "kafka"

logger = Logger.new(STDOUT)
#logger.level = Logger::DEBUG
logger.level = Logger::INFO

brokers = "XXX:XX,XXX:XX" 
topic = "XXX"
consumerGroup = "XXX"


kafka = Kafka.new(
        seed_brokers: brokers,
        client_id: "test",
        socket_timeout: 20,
        logger: logger,
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
