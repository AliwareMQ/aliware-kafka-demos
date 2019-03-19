# fluent-plugin-kafka, a plugin for [Fluentd](http://fluentd.org)

[![Build Status](https://travis-ci.org/htgc/fluent-plugin-kafka.svg?branch=master)](https://travis-ci.org/htgc/fluent-plugin-kafka)

A fluentd plugin to both consume and produce data for Apache Kafka.

TODO: Also, I need to write tests

## Installation

Add this line to your application's Gemfile:

    gem 'fluent-plugin-kafka'

And then execute:

    $ bundle

Or install it yourself as:

    $ gem install fluent-plugin-kafka

If you want to use zookeeper related parameters, you also need to install zookeeper gem. zookeeper gem includes native extension, so development tools are needed, e.g. gcc, make and etc.

## Requirements

- Ruby 2.1 or later
- Input plugins work with kafka v0.9 or later
- Output plugins work with kafka v0.8 or later

## Usage

### Common parameters

#### SSL authentication

- ssl_ca_cert
- ssl_client_cert
- ssl_client_cert_key

Set path to SSL related files. See [Encryption and Authentication using SSL](https://github.com/zendesk/ruby-kafka#encryption-and-authentication-using-ssl) for more detail.

#### SASL authentication

- principal
- keytab

Set principal and path to keytab for SASL/GSSAPI authentication. See [Authentication using SASL](https://github.com/zendesk/ruby-kafka#authentication-using-sasl) for more details.

### Input plugin (@type 'kafka')

Consume events by single consumer.

    <source>
      @type kafka

      brokers <broker1_host>:<broker1_port>,<broker2_host>:<broker2_port>,..
      topics <listening topics(separate with comma',')>
      format <input text type (text|json|ltsv|msgpack)> :default => json
      message_key <key (Optional, for text format only, default is message)>
      add_prefix <tag prefix (Optional)>
      add_suffix <tag suffix (Optional)>

      # Optionally, you can manage topic offset by using zookeeper
      offset_zookeeper    <zookeer node list (<zookeeper1_host>:<zookeeper1_port>,<zookeeper2_host>:<zookeeper2_port>,..)>
      offset_zk_root_node <offset path in zookeeper> default => '/fluent-plugin-kafka'

      # ruby-kafka consumer options
      max_bytes     (integer) :default => nil (Use default of ruby-kafka)
      max_wait_time (integer) :default => nil (Use default of ruby-kafka)
      min_bytes     (integer) :default => nil (Use default of ruby-kafka)
    </source>

Supports a start of processing from the assigned offset for specific topics.

    <source>
      @type kafka

      brokers <broker1_host>:<broker1_port>,<broker2_host>:<broker2_port>,..
      format <input text type (text|json|ltsv|msgpack)>
      <topic>
        topic     <listening topic>
        partition <listening partition: default=0>
        offset    <listening start offset: default=-1>
      </topic>
      <topic>
        topic     <listening topic>
        partition <listening partition: default=0>
        offset    <listening start offset: default=-1>
      </topic>
    </source>

See also [ruby-kafka README](https://github.com/zendesk/ruby-kafka#consuming-messages-from-kafka) for more detailed documentation about ruby-kafka.

Consuming topic name is used for event tag. So when the target topic name is `app_event`, the tag is `app_event`. If you want to modify tag, use `add_prefix` or `add_suffix` parameters. With `add_prefix kafka`, the tag is `kafka.app_event`.

### Input plugin (@type 'kafka_group', supports kafka group)

Consume events by kafka consumer group features..

    <source>
      @type kafka_group

      brokers <broker1_host>:<broker1_port>,<broker2_host>:<broker2_port>,..
      consumer_group <consumer group name, must set>
      topics <listening topics(separate with comma',')>
      format <input text type (text|json|ltsv|msgpack)> :default => json
      message_key <key (Optional, for text format only, default is message)>
      add_prefix <tag prefix (Optional)>
      add_suffix <tag suffix (Optional)>
      retry_emit_limit <Wait retry_emit_limit x 1s when BuffereQueueLimitError happens. The default is nil and it means waiting until BufferQueueLimitError is resolved>
      use_record_time <If true, replace event time with contents of 'time' field of fetched record>
      time_format <string (Optional when use_record_time is used)>

      # ruby-kafka consumer options
      max_bytes               (integer) :default => 1048576
      max_wait_time           (integer) :default => nil (Use default of ruby-kafka)
      min_bytes               (integer) :default => nil (Use default of ruby-kafka)
      offset_commit_interval  (integer) :default => nil (Use default of ruby-kafka)
      offset_commit_threshold (integer) :default => nil (Use default of ruby-kafka)
      start_from_beginning    (bool)    :default => true
    </source>

See also [ruby-kafka README](https://github.com/zendesk/ruby-kafka#consuming-messages-from-kafka) for more detailed documentation about ruby-kafka options.

Consuming topic name is used for event tag. So when the target topic name is `app_event`, the tag is `app_event`. If you want to modify tag, use `add_prefix` or `add_suffix` parameter. With `add_prefix kafka`, the tag is `kafka.app_event`.

### Buffered output plugin

This plugin uses ruby-kafka producer for writing data. This plugin works with recent kafka versions.

    <match *.**>
      @type kafka_buffered

      # Brokers: you can choose either brokers or zookeeper. If you are not familiar with zookeeper, use brokers parameters.
      brokers             <broker1_host>:<broker1_port>,<broker2_host>:<broker2_port>,.. # Set brokers directly
      zookeeper           <zookeeper_host>:<zookeeper_port> # Set brokers via Zookeeper
      zookeeper_path      <broker path in zookeeper> :default => /brokers/ids # Set path in zookeeper for kafka

      default_topic         (string) :default => nil
      default_partition_key (string) :default => nil
      default_message_key   (string) :default => nil
      output_data_type      (json|ltsv|msgpack|attr:<record name>|<formatter name>) :default => json
      output_include_tag    (bool) :default => false
      output_include_time   (bool) :default => false
      exclude_topic_key     (bool) :default => false
      exclude_partition_key (bool) :default => false
      get_kafka_client_log  (bool) :default => false

      # See fluentd document for buffer related parameters: http://docs.fluentd.org/articles/buffer-plugin-overview

      # ruby-kafka producer options
      max_send_retries             (integer)     :default => 1
      required_acks                (integer)     :default => -1
      ack_timeout                  (integer)     :default => nil (Use default of ruby-kafka)
      compression_codec            (gzip|snappy) :default => nil (No compression)
      kafka_agg_max_bytes          (integer)     :default => 4096
      kafka_agg_max_messages       (integer)     :default => nil (No limit)
      max_send_limit_bytes         (integer)     :default => nil (No drop)
      discard_kafka_delivery_failed   (bool)     :default => false (No discard)
      monitoring_list              (array)       :default => []
    </match>

`<formatter name>` of `output_data_type` uses fluentd's formatter plugins. See [formatter article](http://docs.fluentd.org/articles/formatter-plugin-overview).

ruby-kafka sometimes returns `Kafka::DeliveryFailed` error without good information.
In this case, `get_kafka_client_log` is useful for identifying the error cause.
ruby-kafka's log is routed to fluentd log so you can see ruby-kafka's log in fluentd logs.

Supports following ruby-kafka's producer options.

- max_send_retries - default: 1 - Number of times to retry sending of messages to a leader.
- required_acks - default: -1 - The number of acks required per request. If you need flush performance, set lower value, e.g. 1, 2.
- ack_timeout - default: nil - How long the producer waits for acks. The unit is seconds.
- compression_codec - default: nil - The codec the producer uses to compress messages.
- kafka_agg_max_bytes - default: 4096 - Maximum value of total message size to be included in one batch transmission.
- kafka_agg_max_messages - default: nil - Maximum number of messages to include in one batch transmission.
- max_send_limit_bytes - default: nil - Max byte size to send message to avoid MessageSizeTooLarge. For example, if you set 1000000(message.max.bytes in kafka), Message more than 1000000 byes will be dropped.
- discard_kafka_delivery_failed - default: false - discard the record where [Kafka::DeliveryFailed](http://www.rubydoc.info/gems/ruby-kafka/Kafka/DeliveryFailed) occurred
- monitoring_list - default: [] - library to be used to monitor. statsd and datadog are supported

If you want to know about detail of monitoring, see also https://github.com/zendesk/ruby-kafka#monitoring

See also [Kafka::Client](http://www.rubydoc.info/gems/ruby-kafka/Kafka/Client) for more detailed documentation about ruby-kafka.

This plugin supports compression codec "snappy" also.
Install snappy module before you use snappy compression.

    $ gem install snappy

snappy gem uses native extension, so you need to install several packages before.
On Ubuntu, need development packages and snappy library.

    $ sudo apt-get install build-essential autoconf automake libtool libsnappy-dev

#### Load balancing

Messages will be assigned a partition at random as default by ruby-kafka, but messages with the same partition key will always be assigned to the same partition by setting `default_partition_key` in config file.
If key name `partition_key` exists in a message, this plugin set its value of partition_key as key.

|default_partition_key|partition_key| behavior |
| --- | --- | --- |
|Not set|Not exists| All messages are assigned a partition at random |
|Set| Not exists| All messages are assigned to the specific partition |
|Not set| Exists | Messages which have partition_key record are assigned to the specific partition, others are assigned a partition at random |
|Set| Exists | Messages which have partition_key record are assigned to the specific partition with parition_key, others are assigned to the specific partition with default_parition_key |

If key name `message_key` exists in a message, this plugin publishes the value of message_key to kafka and can be read by consumers. Same message key will be assigned to all messages by setting `default_message_key` in config file. If message_key exists and if partition_key is not set explicitly, messsage_key will be used for partitioning.

### Non-buffered output plugin

This plugin uses ruby-kafka producer for writing data. For performance and reliability concerns, use `kafka_bufferd` output instead. This is mainly for testing.

    <match *.**>
      @type kafka

      # Brokers: you can choose either brokers or zookeeper.
      brokers        <broker1_host>:<broker1_port>,<broker2_host>:<broker2_port>,.. # Set brokers directly
      zookeeper      <zookeeper_host>:<zookeeper_port> # Set brokers via Zookeeper
      zookeeper_path <broker path in zookeeper> :default => /brokers/ids # Set path in zookeeper for kafka

      default_topic         (string) :default => nil
      default_partition_key (string) :default => nil
      default_message_key   (string) :default => nil
      output_data_type      (json|ltsv|msgpack|attr:<record name>|<formatter name>) :default => json
      output_include_tag    (bool) :default => false
      output_include_time   (bool) :default => false
      exclude_topic_key     (bool) :default => false
      exclude_partition_key (bool) :default => false

      # ruby-kafka producer options
      max_send_retries    (integer)     :default => 1
      required_acks       (integer)     :default => -1
      ack_timeout         (integer)     :default => nil (Use default of ruby-kafka) 
      compression_codec   (gzip|snappy) :default => nil
      max_buffer_size     (integer)     :default => nil (Use default of ruby-kafka)
      max_buffer_bytesize (integer)     :default => nil (Use default of ruby-kafka) 
    </match>

This plugin also supports ruby-kafka related parameters. See Buffered output plugin section.

## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Added some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request
