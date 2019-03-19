require 'fluent/input'
require 'fluent/time'
require 'fluent/plugin/kafka_plugin_util'

class Fluent::KafkaGroupInput < Fluent::Input
  Fluent::Plugin.register_input('kafka_group', self)

  config_param :brokers, :string, :default => 'localhost:9092',
               :desc => "List of broker-host:port, separate with comma, must set."
  config_param :consumer_group, :string,
               :desc => "Consumer group name, must set."
  config_param :topics, :string,
               :desc => "Listening topics(separate with comma',')."
  config_param :client_id, :string, :default => 'kafka'               
  config_param :format, :string, :default => 'json',
               :desc => "Supported format: (json|text|ltsv|msgpack)"
  config_param :message_key, :string, :default => 'message',
               :desc => "For 'text' format only."
  config_param :add_prefix, :string, :default => nil,
               :desc => "Tag prefix (Optional)"
  config_param :add_suffix, :string, :default => nil,
               :desc => "Tag suffix (Optional)"
  config_param :retry_emit_limit, :integer, :default => nil,
               :desc => "How long to stop event consuming when BufferQueueLimitError happens. Wait retry_emit_limit x 1s. The default is waiting until BufferQueueLimitError is resolved"
  config_param :use_record_time, :bool, :default => false,
               :desc => "Replace message timestamp with contents of 'time' field."
  config_param :time_format, :string, :default => nil,
               :desc => "Time format to be used to parse 'time' filed."
  config_param :kafka_message_key, :string, :default => nil,
               :desc => "Set kafka's message key to this field"

  config_param :retry_wait_seconds, :integer, :default => 30
  # Kafka consumer options
  config_param :max_bytes, :integer, :default => 1048576,
               :desc => "Maximum number of bytes to fetch."
  config_param :max_wait_time, :integer, :default => nil,
               :desc => "How long to block until the server sends us data."
  config_param :min_bytes, :integer, :default => nil,
               :desc => "Smallest amount of data the server should send us."
  config_param :session_timeout, :integer, :default => nil,
               :desc => "The number of seconds after which, if a client hasn't contacted the Kafka cluster"
  config_param :offset_commit_interval, :integer, :default => nil,
               :desc => "The interval between offset commits, in seconds"
  config_param :offset_commit_threshold, :integer, :default => nil,
               :desc => "The number of messages that can be processed before their offsets are committed"
  config_param :start_from_beginning, :bool, :default => true,
               :desc => "Whether to start from the beginning of the topic or just subscribe to new messages being produced"

  include Fluent::KafkaPluginUtil::SSLSettings
  include Fluent::KafkaPluginUtil::SaslSettings

  class ForShutdown < StandardError
  end

  BufferError = if defined?(Fluent::Plugin::Buffer::BufferOverflowError)
                  Fluent::Plugin::Buffer::BufferOverflowError
                else
                  Fluent::BufferQueueLimitError
                end

  unless method_defined?(:router)
    define_method("router") { Fluent::Engine }
  end

  def initialize
    super
    require 'kafka'

    @time_parser = nil
  end

  def _config_to_array(config)
    config_array = config.split(',').map {|k| k.strip }
    if config_array.empty?
      raise Fluent::ConfigError, "kafka_group: '#{config}' is a required parameter"
    end
    config_array
  end

  def multi_workers_ready?
    true
  end

  private :_config_to_array

  def configure(conf)
    super

    $log.info "Will watch for topics #{@topics} at brokers " \
              "#{@brokers} and '#{@consumer_group}' group"

    @topics = _config_to_array(@topics)

    if conf['max_wait_ms']
      log.warn "'max_wait_ms' parameter is deprecated. Use second unit 'max_wait_time' instead"
      @max_wait_time = conf['max_wait_ms'].to_i / 1000
    end

    @parser_proc = setup_parser

    @consumer_opts = {:group_id => @consumer_group}
    @consumer_opts[:session_timeout] = @session_timeout if @session_timeout
    @consumer_opts[:offset_commit_interval] = @offset_commit_interval if @offset_commit_interval
    @consumer_opts[:offset_commit_threshold] = @offset_commit_threshold if @offset_commit_threshold

    @fetch_opts = {}
    @fetch_opts[:max_wait_time] = @max_wait_time if @max_wait_time
    @fetch_opts[:min_bytes] = @min_bytes if @min_bytes

    if @use_record_time and @time_format
      @time_parser = Fluent::TextParser::TimeParser.new(@time_format)
    end
  end

  def setup_parser
    case @format
    when 'json'
      begin
        require 'oj'
        Oj.default_options = Fluent::DEFAULT_OJ_OPTIONS
        Proc.new { |msg| Oj.load(msg.value) }
      rescue LoadError
        require 'yajl'
        Proc.new { |msg| Yajl::Parser.parse(msg.value) }
      end
    when 'ltsv'
      require 'ltsv'
      Proc.new { |msg| LTSV.parse(msg.value, {:symbolize_keys => false}).first }
    when 'msgpack'
      require 'msgpack'
      Proc.new { |msg| MessagePack.unpack(msg.value) }
    when 'text'
      Proc.new { |msg| {@message_key => msg.value} }
    end
  end

  def start
    super

    @kafka = Kafka.new(seed_brokers: @brokers, client_id: @client_id,
                       ssl_ca_cert: read_ssl_file(@ssl_ca_cert),
                       ssl_client_cert: read_ssl_file(@ssl_client_cert),
                       ssl_client_cert_key: read_ssl_file(@ssl_client_cert_key),
                       sasl_gssapi_principal: @principal, sasl_gssapi_keytab: @keytab)
    @consumer = setup_consumer
    @thread = Thread.new(&method(:run))
  end

  def shutdown
    # This nil assignment should be guarded by mutex in multithread programming manner.
    # But the situation is very low contention, so we don't use mutex for now.
    # If the problem happens, we will add a guard for consumer.
    consumer = @consumer
    @consumer = nil
    consumer.stop

    @thread.join
    @kafka.close
    super
  end

  def setup_consumer
    consumer = @kafka.consumer(@consumer_opts)
    @topics.each { |topic|
      consumer.subscribe(topic, start_from_beginning: @start_from_beginning, max_bytes_per_partition: @max_bytes)
    }
    consumer
  end
  
  def reconnect_consumer
    log.warn "Stopping Consumer"
    consumer = @consumer
    @consumer = nil
    consumer.stop
    log.warn "Could not connect to broker. Next retry will be in #{@retry_wait_seconds} seconds"
    sleep @retry_wait_seconds
    @consumer = setup_consumer
    log.warn "Re-starting consumer #{Time.now.to_s}"
  rescue =>e
    log.error "unexpected error during re-starting consumer object access", :error => e.to_s
    log.error_backtrace
  end
  
  def run
    while @consumer
      begin
        @consumer.each_batch(@fetch_opts) { |batch|
          es = Fluent::MultiEventStream.new
          tag = batch.topic
          tag = @add_prefix + "." + tag if @add_prefix
          tag = tag + "." + @add_suffix if @add_suffix

          batch.messages.each { |msg|
            begin
              record = @parser_proc.call(msg)
              if @use_record_time
                if @time_format
                  record_time = @time_parser.parse(record['time'])
                else
                  record_time = record['time']
                end
              else
                record_time = Fluent::Engine.now
              end
              if @kafka_message_key
                record[@kafka_message_key] = msg.key
              end
              es.add(record_time, record)
            rescue => e
              log.warn "parser error in #{batch.topic}/#{batch.partition}", :error => e.to_s, :value => msg.value, :offset => msg.offset
              log.debug_backtrace
            end
          }

          unless es.empty?
            emit_events(tag, es)
          end
        }
      rescue ForShutdown
      rescue => e
        log.error "unexpected error during consuming events from kafka. Re-fetch events.", :error => e.to_s
        log.error_backtrace
        reconnect_consumer
      end
    end
  rescue => e
    log.error "unexpected error during consumer object access", :error => e.to_s
    log.error_backtrace
  end

  def emit_events(tag, es)
    retries = 0
    begin
      router.emit_stream(tag, es)
    rescue BufferError
      raise ForShutdown if @consumer.nil?

      if @retry_emit_limit.nil?
        sleep 1
        retry
      end

      if retries < @retry_emit_limit
        retries += 1
        sleep 1
        retry
      else
        raise RuntimeError, "Exceeds retry_emit_limit"
      end
    end
  end
end
