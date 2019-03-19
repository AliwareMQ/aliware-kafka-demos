require 'fluent/output'
require 'fluent/plugin/kafka_plugin_util'

class Fluent::KafkaOutput < Fluent::Output
  Fluent::Plugin.register_output('kafka', self)

  config_param :brokers, :string, :default => 'localhost:9092',
               :desc => <<-DESC
Set brokers directly
<broker1_host>:<broker1_port>,<broker2_host>:<broker2_port>,..
Note that you can choose to use either brokers or zookeeper.
DESC
  config_param :zookeeper, :string, :default => nil,
               :desc => "Set brokers via Zookeeper: <zookeeper_host>:<zookeeper_port>"
  config_param :zookeeper_path, :string, :default => '/brokers/ids',
               :desc => "Path in path for Broker id. Default to /brokers/ids"
  config_param :default_topic, :string, :default => nil,
               :desc => "Output topic."
  config_param :default_message_key, :string, :default => nil
  config_param :default_partition_key, :string, :default => nil
  config_param :default_partition, :integer, :default => nil
  config_param :client_id, :string, :default => 'kafka'
  config_param :output_data_type, :string, :default => 'json',
               :desc => "Supported format: (json|ltsv|msgpack|attr:<record name>|<formatter name>)"
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
Set true to remove message key from data
DESC
  config_param :exclude_topic_key, :bool, :default => false,
                         :desc => <<-DESC
Set true to remove topic name key from data
DESC

  # ruby-kafka producer options
  config_param :max_send_retries, :integer, :default => 2,
               :desc => "Number of times to retry sending of messages to a leader."
  config_param :required_acks, :integer, :default => -1,
               :desc => "The number of acks required per request."
  config_param :ack_timeout, :integer, :default => nil,
               :desc => "How long the producer waits for acks."
  config_param :compression_codec, :string, :default => nil,
               :desc => "The codec the producer uses to compress messages."

  config_param :time_format, :string, :default => nil

  config_param :max_buffer_size, :integer, :default => nil,
               :desc => "Number of messages to be buffered by the kafka producer."

  config_param :max_buffer_bytesize, :integer, :default => nil,
               :desc => "Maximum size in bytes to be buffered."

  config_param :active_support_notification_regex, :string, :default => nil,
               :desc => <<-DESC
Add a regular expression to capture ActiveSupport notifications from the Kafka client
requires activesupport gem - records will be generated under fluent_kafka_stats.**
DESC

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

    @kafka = nil
  end

  def refresh_client
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
        @kafka = Kafka.new(seed_brokers: @seed_brokers, client_id: @client_id, ssl_ca_cert: read_ssl_file(@ssl_ca_cert),
                           ssl_client_cert: read_ssl_file(@ssl_client_cert), ssl_client_cert_key: read_ssl_file(@ssl_client_cert_key),
                           sasl_gssapi_principal: @principal, sasl_gssapi_keytab: @keytab)
        log.info "initialized kafka producer: #{@client_id}"
      else
        log.warn "No brokers found on Zookeeper"
      end
    rescue Exception => e
      log.error e
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
    @producer_opts[:max_buffer_size] = @max_buffer_size if @max_buffer_size
    @producer_opts[:max_buffer_bytesize] = @max_buffer_bytesize if @max_buffer_bytesize
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

  def shutdown
    super
    @kafka = nil
  end

  def setup_formatter(conf)
    if @output_data_type == 'json'
      require 'yajl'
      Proc.new { |tag, time, record| Yajl::Encoder.encode(record) }
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

  def emit(tag, es, chain)
    begin
      chain.next

      # out_kafka is mainly for testing so don't need the performance unlike out_kafka_buffered.
      producer = @kafka.producer(@producer_opts)

      es.each do |time, record|
        if @output_include_time
          if @time_format
            record['time'] = Time.at(time).strftime(@time_format)
          else
            record['time'] = time
          end
        end
        record['tag'] = tag if @output_include_tag
        topic = (@exclude_topic_key ? record.delete('topic') : record['topic']) || @default_topic || tag
        partition_key = (@exclude_partition_key ? record.delete('partition_key') : record['partition_key']) || @default_partition_key
        partition = (@exclude_partition ? record.delete('partition'.freeze) : record['partition'.freeze]) || @default_partition
        message_key = (@exclude_message_key ? record.delete('message_key') : record['message_key']) || @default_message_key

        value = @formatter_proc.call(tag, time, record)

        log.trace { "message will send to #{topic} with partition_key: #{partition_key}, partition: #{partition}, message_key: #{message_key} and value: #{value}." }
	begin
          producer.produce(value, topic: topic, key: message_key, partition: partition, partition_key: partition_key)
	rescue Kafka::BufferOverflow => e
	  log.warn "BufferOverflow occurred: #{e}"
	  log.info "Trying to deliver the messages to prevent the buffer from overflowing again."
	  producer.deliver_messages
	  log.info "Recovered from BufferOverflow successfully`"
	end
      end

      producer.deliver_messages
      producer.shutdown
    rescue Exception => e
      log.warn "Send exception occurred: #{e}"
      producer.shutdown if producer
      refresh_client
      raise e
    end
  end

end
