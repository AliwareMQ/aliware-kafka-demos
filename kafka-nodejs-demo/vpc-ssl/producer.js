
const Kafka = require('node-rdkafka');
const config = require('./setting');
console.log("features:" + Kafka.features);
console.log(Kafka.librdkafkaVersion);

var producer = new Kafka.Producer({
	/*'debug': 'all', */
    'api.version.request': 'true',
    'bootstrap.servers': config['bootstrap_servers'],
    'dr_cb': true,
    'dr_msg_cb': true,
    'security.protocol' : 'sasl_ssl',
	'ssl.ca.location' : './mix-4096-ca-cert',
	'sasl.mechanisms' : 'PLAIN',
    'ssl.endpoint.identification.algorithm':'none',
	'sasl.username' : config['sasl_plain_username'],
	'sasl.password' : config['sasl_plain_password']
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

  });

function produce() {
	//连接成功后，尝试发一条消息
  try {
    producer.produce(
      // Topic to send the message to
      config['topic_name'],
      // optionally we can manually specify a partition for the message
      // this defaults to -1 - which will use librdkafka's default partitioner (consistent random for keyed messages, random for unkeyed messages)
      null,
      // Message to send. Must be a buffer
      new Buffer('Hello Ali Kafka'),
      // for keyed messages, we also specify the key - note that this field is optional
      null,
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
}

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
    console.error('event.error:' + err);
})

//每隔1s发送一条消息，这里是测试，生产环境请按需发送
setInterval(produce,1000,"Interval");

