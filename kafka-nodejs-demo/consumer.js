
const Kafka = require('node-rdkafka');
console.log(Kafka.features);
console.log(Kafka.librdkafkaVersion);

var config = {
  'bootstrap.servers' : 'kafka-ons-internet.aliyun.com:8080', //各个region不一样
  'sasl.username' : 'XXX',
  'sasl.password' : 'XXX',
  'ssl.ca.location' : './ca-cert',
  'topic' : 'XXX',
  'group.id' : 'XXX'
}

var consumer = new Kafka.KafkaConsumer({
	/*'debug': 'all', */
  'api.version.request': 'true',
	'bootstrap.servers': config['bootstrap.servers'],
	'security.protocol' : 'sasl_ssl',
	'ssl.ca.location' : config['ssl.ca.location'],
	'sasl.mechanisms' : 'PLAIN',
	'sasl.username' : config['sasl.username'],
	'sasl.password' : config['sasl.password'],
  'group.id' : config['group.id']
});


// Flowing mode:
consumer.connect();

consumer.on('ready', function() {
  console.log("connect ok");
  
  consumer.subscribe([config['topic']]);

  // Consume from the librdtesting-01 topic. This is what determines
  // the mode we are running in. By not specifying a callback (or specifying
  // only a callback) we get messages as soon as they are available.
  consumer.consume();
})

consumer.on('data', function(data) {
  // Output the actual message contents
  console.log(data);
});


consumer.on('event.log', function(event) {
      console.log("event.log", event);
});

consumer.on("error", function(error) {
	console.log("error:" + error);
});



