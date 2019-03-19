require "set"
require "kafka/partitioner"
require "kafka/message_buffer"
require "kafka/produce_operation"
require "kafka/pending_message_queue"
require "kafka/pending_message"
require "kafka/compressor"
require 'kafka/producer'

# for out_kafka_buffered
module Kafka
  class Producer
    def produce2(value, key: nil, topic:, partition: nil, partition_key: nil)
      create_time = Time.now

      message = PendingMessage.new(
        value,
        key,
        topic,
        partition,
        partition_key,
        create_time
      )

      @target_topics.add(topic)
      @pending_message_queue.write(message)

      nil
    end
  end
end

# for out_kafka2
module Kafka
  class Client
    def topic_producer(topic, compression_codec: nil, compression_threshold: 1, ack_timeout: 5, required_acks: :all, max_retries: 2, retry_backoff: 1, max_buffer_size: 1000, max_buffer_bytesize: 10_000_000)
      compressor = Compressor.new(
        codec_name: compression_codec,
        threshold: compression_threshold,
        instrumenter: @instrumenter,
      )

      TopicProducer.new(topic,
        cluster: initialize_cluster,
        logger: @logger,
        instrumenter: @instrumenter,
        compressor: compressor,
        ack_timeout: ack_timeout,
        required_acks: required_acks,
        max_retries: max_retries,
        retry_backoff: retry_backoff,
        max_buffer_size: max_buffer_size,
        max_buffer_bytesize: max_buffer_bytesize,
      )
    end
  end

  class TopicProducer
    def initialize(topic, cluster:, logger:, instrumenter:, compressor:, ack_timeout:, required_acks:, max_retries:, retry_backoff:, max_buffer_size:, max_buffer_bytesize:)
      @cluster = cluster
      @logger = logger
      @instrumenter = instrumenter
      @required_acks = required_acks == :all ? -1 : required_acks
      @ack_timeout = ack_timeout
      @max_retries = max_retries
      @retry_backoff = retry_backoff
      @max_buffer_size = max_buffer_size
      @max_buffer_bytesize = max_buffer_bytesize
      @compressor = compressor

      @topic = topic
      @cluster.add_target_topics(Set.new([topic]))

      # A buffer organized by topic/partition.
      @buffer = MessageBuffer.new

      # Messages added by `#produce` but not yet assigned a partition.
      @pending_message_queue = PendingMessageQueue.new
    end

    def produce(value, key, partition, partition_key)
      create_time = Time.now

      message = PendingMessage.new(
        value,
        key,
        @topic,
        partition,
        partition_key,
        create_time
      )

      @pending_message_queue.write(message)

      nil
    end

    def deliver_messages
      # There's no need to do anything if the buffer is empty.
      return if buffer_size == 0

      deliver_messages_with_retries
    end

    # Returns the number of messages currently held in the buffer.
    #
    # @return [Integer] buffer size.
    def buffer_size
      @pending_message_queue.size + @buffer.size
    end

    def buffer_bytesize
      @pending_message_queue.bytesize + @buffer.bytesize
    end

    # Deletes all buffered messages.
    #
    # @return [nil]
    def clear_buffer
      @buffer.clear
      @pending_message_queue.clear
    end

    # Closes all connections to the brokers.
    #
    # @return [nil]
    def shutdown
      @cluster.disconnect
    end

    private

    def deliver_messages_with_retries
      attempt = 0

      #@cluster.add_target_topics(@target_topics)

      operation = ProduceOperation.new(
        cluster: @cluster,
        buffer: @buffer,
        required_acks: @required_acks,
        ack_timeout: @ack_timeout,
        compressor: @compressor,
        logger: @logger,
        instrumenter: @instrumenter,
      )

      loop do
        attempt += 1

        @cluster.refresh_metadata_if_necessary!

        assign_partitions!
        operation.execute

        if @required_acks.zero?
          # No response is returned by the brokers, so we can't know which messages
          # have been successfully written. Our only option is to assume that they all
          # have.
          @buffer.clear
        end

        if buffer_size.zero?
          break
        elsif attempt <= @max_retries
          @logger.warn "Failed to send all messages; attempting retry #{attempt} of #{@max_retries} after #{@retry_backoff}s"

          sleep @retry_backoff
        else
          @logger.error "Failed to send all messages; keeping remaining messages in buffer"
          break
        end
      end

      unless @pending_message_queue.empty?
        # Mark the cluster as stale in order to force a cluster metadata refresh.
        @cluster.mark_as_stale!
        raise DeliveryFailed, "Failed to assign partitions to #{@pending_message_queue.size} messages"
      end

      unless @buffer.empty?
        partitions = @buffer.map {|topic, partition, _| "#{topic}/#{partition}" }.join(", ")

        raise DeliveryFailed, "Failed to send messages to #{partitions}"
      end
    end

    def assign_partitions!
      failed_messages = []
      partition_count = @cluster.partitions_for(@topic).count

      @pending_message_queue.each do |message|
        partition = message.partition

        begin
          if partition.nil?
            partition = Partitioner.partition_for_key(partition_count, message)
          end

          @buffer.write(
            value: message.value,
            key: message.key,
            topic: message.topic,
            partition: partition,
            create_time: message.create_time,
          )
        rescue Kafka::Error => e
          failed_messages << message
        end
      end

      if failed_messages.any?
        failed_messages.group_by(&:topic).each do |topic, messages|
          @logger.error "Failed to assign partitions to #{messages.count} messages in #{topic}"
        end

        @cluster.mark_as_stale!
      end

      @pending_message_queue.replace(failed_messages)
    end
  end
end
