# Consumes lines from a Kafka partition and writes them to STDOUT.
#
$LOAD_PATH.unshift(File.expand_path("../../lib", __FILE__))

require "kafka"

# We don't want log output to clutter the console. Replace `StringIO.new`
# with e.g. `$stderr` if you want to see what's happening under the hood.
logger = Logger.new(StringIO.new)

brokers = "XXXX" # 控制台上获取的kafka实例的接入地址

# 注意： 以下三个配置项只有在SSL接入点时才需要配置，对于VPC内直连访问不需要设置
ssl_ca_cert_file = '../../alikafa-sasl.cert' # SASL证书文件，在本demo目录下
sasl_plain_username = "XXXX" # 控制台上kafka实例配置信息中用户名
sasl_plain_password = "XXXX" # 控制台上kafka实例配置信息中密码

# Make sure to create this topic in your Kafka cluster 
topic = "XXXX"  # 在控制台kafka实例上提前创建的测试Topic
groupId = "XXXX"  # 在控制台kafka实例上提前创建的测试消费组



puts "bootstrap.servers= #{brokers}"
puts "consume topic= `#{topic}`"
puts "consume group= `#{groupId}`"

kafka = Kafka.new(
  seed_brokers: brokers,
  client_id: "simple-consumer",
  socket_timeout: 20,
  logger: logger,
  # 注意：以下三个参数只有在SSL接入点时才需要设置
  ssl_ca_cert:  File.read(ssl_ca_cert_file),
  sasl_plain_username: sasl_plain_username,
  sasl_plain_password: sasl_plain_password,
)

consumer = kafka.consumer(group_id: groupId)
consumer.subscribe(topic)

trap("TERM") { consumer.stop }

#consumer.each_message(topic: topic) do |message|
#  puts message.value
#end

puts "-- Consume Result --"
consumer.each_message do |message|
  puts "topic-partition: #{message.topic}/#{message.partition} offset=#{message.offset}, key=#{message.key}, value=#{message.value}"
end
