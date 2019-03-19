# Reads lines from STDIN, writing them to Kafka.
#
$LOAD_PATH.unshift(File.expand_path("../../lib", __FILE__))

require "kafka"

logger = Logger.new($stderr)

brokers = "XXXX" # 控制台上获取的kafka实例的接入地址

# 注意： 以下三个配置项只有在SSL接入点时才需要配置，对于VPC内直连访问不需要设置
ssl_ca_cert_file = '../../alikafa-sasl`.cert' # SASL证书文件，在本demo目录下
sasl_plain_username = "XXXX" # 控制台上kafka实例配置信息中用户名
sasl_plain_password = "XXXX" # 控制台上kafka实例配置信息中密码

# Make sure to create this topic in your Kafka cluster
topic = "XXXX"  # 在控制台kafka实例上提前创建的测试Topic

puts "bootstrap.servers= #{brokers}"
puts "producer topic= `#{topic}`"

kafka = Kafka.new(
  seed_brokers: brokers,
  client_id: "simple-producer",
  logger: logger,
  # 注意：以下三个参数只有在SSL接入点时才需要设置
  ssl_ca_cert:  File.read(ssl_ca_cert_file), 
  sasl_plain_username: sasl_plain_username,
  sasl_plain_password: sasl_plain_password,
)

producer = kafka.producer

begin
  $stdin.each_with_index do |line, index|
    producer.produce(line, topic: topic)

    # Send messages for every 3 lines.
    # 每输入三行作为一次批处理发送
    producer.deliver_messages if index % 3 == 0
  end
ensure
  # Make sure to send any remaining messages.
  producer.deliver_messages

  producer.shutdown
end
