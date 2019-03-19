require 'thread'
require 'fluent/output'
require 'fluent/plugin/kafka_plugin_util'

class Fluent::KafkaOutputBuffered < Fluent::BufferedOutput
  Fluent::Plugin.register_output('kafka_buffered', self)

  config_param :brokers, :string, :default => 'localhost:9092',
               :desc => <<-DESC
Set brokers directly:
<broker1_host>:<broker1_port>,<broker2_host>:<broker2_port>,..
Brokers: you can choose to use either brokers or zookeeper.
DESC
  config_param :zookeeper, :string, :default => nil,
               :desc => <<-DESC
Set brokers via Zookeeper:
<zookeeper_host>:<zookeeper_port>
DESC
  config_param :zookeeper_path, :string, :default => '/brokers/ids',
               :desc => "Path in path for Broker id. Default to /brokers/ids"
  config_param :default_topic, :string, :default => nil,
               :desc => "Output topic"
  config_param :default_message_key, :string, :default => nil
  config_param :default_partition_key, :string, :default => nil
  config_param :default_partition, :integer, :default => nil
  config_param :client_id, :string, :default => 'kafka'
  config_param :output_data_type, :string, :default => 'json',
               :desc => <<-DESC
Supported format: (json|ltsv|msgpack|attr:<record name>|<formatter name>)
DESC
  config_param :output_include_tag, :bool, :default => false
  config_param :output_include_time, :bool, :default => false
  config_param :exclude_partition_key, :bool, :default => false,
               :desc => <<-DESC
Set true to remove partition key from data
DESC
  config_param :exclude_partition, :bool, :default => false,
               :desc => <<-DESC
Set true to remove partition from data
DESC
   config_param :exclude_message_key, :bool, :default => false,
               :desc => <<-DESC
Set true to remove partition key from data
DESC
   config_param :exclude_topic_key, :bool, :default => false,
                :desc => <<-DESC
Set true to remove topic name key from data
DESC

  config_param :kafka_agg_max_bytes, :size, :default => 4*1024  #4k
  config_param :kafka_agg_max_messages, :integer, :default => nil
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
  config_param :max_send_limit_bytes, :size, :default => nil
  config_param :discard_kafka_delivery_failed, :bool, :default => false

  config_param :time_format, :string, :default => nil

  config_param :active_support_notification_regex, :string, :default => nil,
               :desc => <<-DESC
Add a regular expression to capture ActiveSupport notifications from the Kafka client
requires activesupport gem - records will be generated under fluent_kafka_stats.**
DESC

  config_param :monitoring_list, :array, :default => [],
               :desc => "library to be used to monitor. statsd and datadog are supported"

  include Fluent::KafkaPluginUtil::SSLSettings
  include Fluent::KafkaPluginUtil::SaslSettings

  attr_accessor :output_data_type
  attr_accessor :field_separator

  unless method_defined?(:log)
    define_method("log") { $log }
  end

  def initialize
    super

    require 'kafka'
    require 'fluent/plugin/kafka_producer_ext'

    @kafka = nil
    @producers = {}
    @producers_mutex = Mutex.new
  end

  def multi_workers_ready?
    true
  end

  def refresh_client(raise_error = true)
    if @zookeeper
      @seed_brokers = []
      z = Zookeeper.new(@zookeeper)
      z.get_children(:path => @zookeeper_path)[:children].each do |id|
        broker = Yajl.load(z.get(:path => @zookeeper_path + "/#{id}")[:data])
        @seed_brokers.push("#{broker['host']}:#{broker['port']}")
      end
      z.close
      log.info "brokers has been refreshed via Zookeeper: #{@seed_brokers}"
    end
    begin
      if @seed_brokers.length > 0
        logger = @get_kafka_client_log ? log : nil
        @kafka = Kafka.new(seed_brokers: @seed_brokers, client_id: @client_id, logger: logger, ssl_ca_cert: read_ssl_file(@ssl_ca_cert),
                           ssl_client_cert: read_ssl_file(@ssl_client_cert), ssl_client_cert_key: read_ssl_file(@ssl_client_cert_key),
                           sasl_gssapi_principal: @principal, sasl_gssapi_keytab: @keytab)
        log.info "initialized kafka producer: #{@client_id}"
      else
        log.warn "No brokers found on Zookeeper"
      end
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

    if @zookeeper
      require 'zookeeper'
    else
      @seed_brokers = @brokers.match(",").nil? ? [@brokers] : @brokers.split(",")
      log.info "brokers has been set directly: #{@seed_brokers}"
    end

    if conf['ack_timeout_ms']
      log.warn "'ack_timeout_ms' parameter is deprecated. Use second unit 'ack_timeout' instead"
      @ack_timeout = conf['ack_timeout_ms'].to_i / 1000
    end

    @f_separator = case @field_separator
                   when /SPACE/i then ' '
                   when /COMMA/i then ','
                   when /SOH/i then "\x01"
                   else "\t"
                   end

    @formatter_proc = setup_formatter(conf)

    @producer_opts = {max_retries: @max_send_retries, required_acks: @required_acks}
    @producer_opts[:ack_timeout] = @ack_timeout if @ack_timeout
    @producer_opts[:compression_codec] = @compression_codec.to_sym if @compression_codec

    if @discard_kafka_delivery_failed
      log.warn "'discard_kafka_delivery_failed' option discards events which cause delivery failure, e.g. invalid topic or something."
      log.warn "If this is unexpected, you need to check your configuration or data."
    end

    if @active_support_notification_regex
      require 'active_support/notifications'
      require 'active_support/core_ext/hash/keys'
      ActiveSupport::Notifications.subscribe(Regexp.new(@active_support_notification_regex)) do |*args|
        event = ActiveSupport::Notifications::Event.new(*args)
        message = event.payload.respond_to?(:stringify_keys) ? event.payload.stringify_keys : event.payload
        @router.emit("fluent_kafka_stats.#{event.name}", Time.now.to_i, message)
      end
    end

    @monitoring_list.each { |m|
      require "kafka/#{m}"
      log.info "#{m} monitoring started"
    }
  end

  def start
    super
    refresh_client
  end

  def shutdown
    super
    shutdown_producers
    @kafka = nil
  end

  def emit(tag, es, chain)
    super(tag, es, chain, tag)
  end

  def format_stream(tag, es)
    es.to_msgpack_stream
  end

  def shutdown_producers
    @producers_mutex.synchronize {
      @producers.each { |key, producer|
        producer.shutdown
      }
      @producers = {}
    }
  end

  def get_producer
    @producers_mutex.synchronize {
      producer = @producers[Thread.current.object_id]
      unless producer
        producer = @kafka.producer(@producer_opts)
        @producers[Thread.current.object_id] = producer
      end
      producer
    }
  end

  def setup_formatter(conf)
    if @output_data_type == 'json'
      begin
        require 'oj'
        Oj.default_options = Fluent::DEFAULT_OJ_OPTIONS
        Proc.new { |tag, time, record| Oj.dump(record) }
      rescue LoadError
        require 'yajl'
        Proc.new { |tag, time, record| Yajl::Encoder.encode(record) }
      end
    elsif @output_data_type == 'ltsv'
      require 'ltsv'
      Proc.new { |tag, time, record| LTSV.dump(record) }
    elsif @output_data_type == 'msgpack'
      require 'msgpack'
      Proc.new { |tag, time, record| record.to_msgpack }
    elsif @output_data_type =~ /^attr:(.*)$/
      @custom_attributes = $1.split(',').map(&:strip).reject(&:empty?)
      @custom_attributes.unshift('time') if @output_include_time
      @custom_attributes.unshift('tag') if @output_include_tag
      Proc.new { |tag, time, record|
        @custom_attributes.map { |attr|
          record[attr].nil? ? '' : record[attr].to_s
        }.join(@f_separator)
      }
    else
      @formatter = Fluent::Plugin.new_formatter(@output_data_type)
      @formatter.configure(conf)
      @formatter.method(:format)
    end
  end

  def deliver_messages(producer, tag)
    if @discard_kafka_delivery_failed
      begin
        producer.deliver_messages
      rescue Kafka::DeliveryFailed => e
        log.warn "DeliveryFailed occurred. Discard broken event:", :error => e.to_s, :error_class => e.class.to_s, :tag => tag
        producer.clear_buffer
      end
    else
      producer.deliver_messages
    end
  end

  def write(chunk)
    tag = chunk.key
    def_topic = @default_topic || tag
    producer = get_producer

    records_by_topic = {}
    bytes_by_topic = {}
    messages = 0
    messages_bytes = 0
    record_buf = nil
    record_buf_bytes = nil

    begin
      chunk.msgpack_each { |time, record|
        begin
          if @output_include_time
            if @time_format
              record['time'.freeze] = Time.at(time).strftime(@time_format)
            else
              record['time'.freeze] = time
            end
          end

          record['tag'] = tag if @output_include_tag
          topic = (@exclude_topic_key ? record.delete('topic'.freeze) : record['topic'.freeze]) || def_topic
          partition_key = (@exclude_partition_key ? record.delete('partition_key'.freeze) : record['partition_key'.freeze]) || @default_partition_key
          partition = (@exclude_partition ? record.delete('partition'.freeze) : record['partition'.freeze]) || @default_partition
          message_key = (@exclude_message_key ? record.delete('message_key'.freeze) : record['message_key'.freeze]) || @default_message_key

          records_by_topic[topic] ||= 0
          bytes_by_topic[topic] ||= 0

          record_buf = @formatter_proc.call(tag, time, record)
          record_buf_bytes = record_buf.bytesize
          if @max_send_limit_bytes && record_buf_bytes > @max_send_limit_bytes
            log.warn "record size exceeds max_send_limit_bytes. Skip event:", :time => time, :record => record
            next
          end
        rescue StandardError => e
          log.warn "unexpected error during format record. Skip broken event:", :error => e.to_s, :error_class => e.class.to_s, :time => time, :record => record
          next
        end

        if (messages > 0) and (messages_bytes + record_buf_bytes > @kafka_agg_max_bytes) or (@kafka_agg_max_messages && messages >= @kafka_agg_max_messages)
          log.debug { "#{messages} messages send because reaches the limit of batch transmission." }
          deliver_messages(producer, tag)
          messages = 0
          messages_bytes = 0
        end
        log.trace { "message will send to #{topic} with partition_key: #{partition_key}, partition: #{partition}, message_key: #{message_key} and value: #{record_buf}." }
        messages += 1
        producer.produce2(record_buf, topic: topic, key: message_key, partition_key: partition_key, partition: partition)
        messages_bytes += record_buf_bytes

        records_by_topic[topic] += 1
        bytes_by_topic[topic] += record_buf_bytes
      }
      if messages > 0
        log.debug { "#{messages} messages send." }
        deliver_messages(producer, tag)
      end
      log.debug { "(records|bytes) (#{records_by_topic}|#{bytes_by_topic})" }
    end
  rescue Exception => e
    log.warn "Send exception occurred: #{e}"
    log.warn "Exception Backtrace : #{e.backtrace.join("\n")}"
    # For safety, refresh client and its producers
    shutdown_producers
    refresh_client(false)
    # Raise exception to retry sendind messages
    raise e
  end
end
