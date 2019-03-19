require 'fluent/plugin/output'
require 'fluent/plugin/kafka_plugin_util'

require 'kafka'
require 'fluent/plugin/kafka_producer_ext'

module Fluent::Plugin
  class Fluent::Kafka2Output < Output
    Fluent::Plugin.register_output('kafka2', self)

    helpers :inject, :formatter

    config_param :brokers, :array, :value_type => :string, :default => ['localhost:9092'],
                 :desc => <<-DESC
Set brokers directly:
<broker1_host>:<broker1_port>,<broker2_host>:<broker2_port>,..
DESC
    config_param :default_topic, :string, :default => nil,
                 :desc => "Default output topic when record doesn't have topic field"
    config_param :default_message_key, :string, :default => nil
    config_param :default_partition_key, :string, :default => nil
    config_param :default_partition, :integer, :default => nil
    config_param :client_id, :string, :default => 'fluentd'
    config_param :exclude_partition_key, :bool, :default => false,
                 :desc => 'Set true to remove partition key from data'
    config_param :exclude_partition, :bool, :default => false,
                 :desc => 'Set true to remove partition from data'
    config_param :exclude_message_key, :bool, :default => false,
                 :desc => 'Set true to remove partition key from data'
    config_param :exclude_topic_key, :bool, :default => false,
                 :desc => 'Set true to remove topic name key from data'

    config_param :get_kafka_client_log, :bool, :default => false

    # ruby-kafka producer options
    config_param :max_send_retries, :integer, :default => 2,
                 :desc => "Number of times to retry sending of messages to a leader."
    config_param :required_acks, :integer, :default => -1,
                 :desc => "The number of acks required per request."
    config_param :ack_timeout, :time, :default => nil,
                 :desc => "How long the producer waits for acks."
    config_param :compression_codec, :string, :default => nil,
                 :desc => <<-DESC
The codec the producer uses to compress messages.
Supported codecs: (gzip|snappy)
DESC

    config_param :active_support_notification_regex, :string, :default => nil,
                 :desc => <<-DESC
Add a regular expression to capture ActiveSupport notifications from the Kafka client
requires activesupport gem - records will be generated under fluent_kafka_stats.**
DESC

    config_section :buffer do
      config_set_default :chunk_keys, ["topic"]
    end
    config_section :format do
      config_set_default :@type, 'json'
    end

    include Fluent::KafkaPluginUtil::SSLSettings
    include Fluent::KafkaPluginUtil::SaslSettings

    def initialize
      super

      @kafka = nil
    end

    def refresh_client(raise_error = true)
      begin
        logger = @get_kafka_client_log ? log : nil
        @kafka = Kafka.new(seed_brokers: @brokers, client_id: @client_id, logger: logger, ssl_ca_cert: read_ssl_file(@ssl_ca_cert),
                           ssl_client_cert: read_ssl_file(@ssl_client_cert), ssl_client_cert_key: read_ssl_file(@ssl_client_cert_key),
                           sasl_gssapi_principal: @principal, sasl_gssapi_keytab: @keytab)
        log.info "initialized kafka producer: #{@client_id}"
      rescue Exception => e
        if raise_error # During startup, error should be reported to engine and stop its phase for safety.
          raise e
        else
          log.error e
        end
      end
    end

    def configure(conf)
      super

      if @brokers.size > 0
        log.info "brokers has been set: #{@brokers}"
      else
        raise Fluent::Config, 'No brokers specified. Need one broker at least.'
      end

      formatter_conf = conf.elements('format').first
      unless formatter_conf
        raise Fluent::ConfigError, "<format> section is required."
      end
      unless formatter_conf["@type"]
        raise Fluent::ConfigError, "format/@type is required."
      end
      @formatter_proc = setup_formatter(formatter_conf)

      if @default_topic.nil?
        if @chunk_keys.include?('topic') && !@chunk_keys.include?('tag')
          log.warn "Use 'topic' field of event record for topic but no fallback. Recommend to set default_topic or set 'tag' in buffer chunk keys like <buffer topic,tag>"
        end
      else
        if @chunk_keys.include?('tag')
          log.warn "default_topic is set. Fluentd's event tag is not used for topic"
        end
      end

      @producer_opts = {max_retries: @max_send_retries, required_acks: @required_acks}
      @producer_opts[:ack_timeout] = @ack_timeout if @ack_timeout
      @producer_opts[:compression_codec] = @compression_codec.to_sym if @compression_codec
      if @active_support_notification_regex
        require 'active_support/notifications'
        require 'active_support/core_ext/hash/keys'
        ActiveSupport::Notifications.subscribe(Regexp.new(@active_support_notification_regex)) do |*args|
          event = ActiveSupport::Notifications::Event.new(*args)
          message = event.payload.respond_to?(:stringify_keys) ? event.payload.stringify_keys : event.payload
          @router.emit("fluent_kafka_stats.#{event.name}", Time.now.to_i, message)
        end
      end
    end

    def multi_workers_ready?
      true
    end

    def start
      super
      refresh_client
    end

    def close
      super
      @kafka.close if @kafka
    end

    def terminate
      super
      @kafka = nil
    end

    def setup_formatter(conf)
      type = conf['@type']
      case type
      when 'json'
        begin
          require 'oj'
          Oj.default_options = Fluent::DEFAULT_OJ_OPTIONS
          Proc.new { |tag, time, record| Oj.dump(record) }
        rescue LoadError
          require 'yajl'
          Proc.new { |tag, time, record| Yajl::Encoder.encode(record) }
        end
      when 'ltsv'
        require 'ltsv'
        Proc.new { |tag, time, record| LTSV.dump(record) }
      else
        @formatter = formatter_create(usage: 'kafka-plugin', conf: conf)
        @formatter.method(:format)
      end
    end

    # TODO: optimize write performance
    def write(chunk)
      tag = chunk.metadata.tag
      topic = chunk.metadata.variables[:topic] || @default_topic || tag
      producer = @kafka.topic_producer(topic, @producer_opts)

      messages = 0
      record_buf = nil

      begin
        chunk.msgpack_each { |time, record|
          begin
            record = inject_values_to_record(tag, time, record)
            record.delete('topic'.freeze) if @exclude_topic_key
            partition_key = (@exclude_partition_key ? record.delete('partition_key'.freeze) : record['partition_key'.freeze]) || @default_partition_key
            partition = (@exclude_partition ? record.delete('partition'.freeze) : record['partition'.freeze]) || @default_partition
            message_key = (@exclude_message_key ? record.delete('message_key'.freeze) : record['message_key'.freeze]) || @default_message_key

            record_buf = @formatter_proc.call(tag, time, record)
          rescue StandardError => e
            log.warn "unexpected error during format record. Skip broken event:", :error => e.to_s, :error_class => e.class.to_s, :time => time, :record => record
            next
          end

          log.trace { "message will send to #{topic} with partition_key: #{partition_key}, partition: #{partition}, message_key: #{message_key} and value: #{record_buf}." }
          messages += 1

          producer.produce(record_buf, message_key, partition, partition_key)
        }

        if messages > 0
          log.debug { "#{messages} messages send." }
          producer.deliver_messages
        end
      end
    rescue Exception => e
      log.warn "Send exception occurred: #{e}"
      log.warn "Exception Backtrace : #{e.backtrace.join("\n")}"
      # For safety, refresh client and its producers
      refresh_client(false)
      # Raise exception to retry sendind messages
      raise e
    ensure
      producer.shutdown if producer
    end
  end
end
