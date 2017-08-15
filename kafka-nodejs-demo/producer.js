
const Kafka = require('node-rdkafka');
console.log(Kafka.features);
console.log(Kafka.librdkafkaVersion);

var config = {
  'bootstrap.servers' : 'kafka-ons-internet.aliyun.com:8080', //各个region不一样
  'sasl.username' : 'XXX',
  'sasl.password' : 'XXX',
  'ssl.ca.location' : './ca-cert',
  'topic' : 'XXX'
}

var producer = new Kafka.Producer({
	/*'debug': 'all', */
    'api.version.request': 'true',
    'bootstrap.servers': config['bootstrap.servers'],
    'dr_cb': true,
    'dr_msg_cb': true,
    'security.protocol' : 'sasl_ssl',
	'ssl.ca.location' : config['ssl.ca.location'],
	'sasl.mechanisms' : 'PLAIN',
	'sasl.username' : config['sasl.username'],
	'sasl.password' : config['sasl.password']
});

var connected = false
// Poll for events every 100 ms
producer.setPollInterval(100);

// Connect to the broker manually
producer.connect();



// Wait for the ready event before proceeding
producer.on('ready', function() {
  connected = true
  console.log("connect ok")

  //连接成功后，尝试发一条消息
  try {
    producer.produce(
      // Topic to send the message to
      config['topic'],
      // optionally we can manually specify a partition for the message
      // this defaults to -1 - which will use librdkafka's default partitioner (consistent random for keyed messages, random for unkeyed messages)
      null,
      // Message to send. Must be a buffer
      new Buffer('Hello Ali Kafka'),
      // for keyed messages, we also specify the key - note that this field is optional
      'Ali',
      // you can send a timestamp here. If your broker version supports it,
      // it will get added. Otherwise, we default to 0
      Date.now()
      // you can send an opaque token here, which gets passed along
      // to your delivery reports
    );
  } catch (err) {
    console.error('A problem occurred when sending our message');
    console.error(err);
  }
});

producer.on("disconnected", function() {
  connected = false;
  //断线自动重连
  producer.connect();
})

producer.on('event.log', function(event) {
      console.log("event.log", event);
});

producer.on("error", function(error) {
	console.log("error:" + error);
});

producer.on('delivery-report', function(err, report) {
  //消息发送成功，这里会收到report
  console.log("delivery-report: producer ok");
});
// Any errors we encounter, including connection errors
producer.on('event.error', function(err) {
  //AliKafka服务器会主动掐掉空闲连接，如果发现这个异常，则客户端重连(先disconnect再connect)
  if (-1 == err.code) {
      producer.disconnect();
  } else {
     console.error('event.error:' + err);
  }
})



