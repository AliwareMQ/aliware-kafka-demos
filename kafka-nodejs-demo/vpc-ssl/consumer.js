
const Kafka = require('node-rdkafka');
const config = require('./setting');
console.log(Kafka.features);
console.log(Kafka.librdkafkaVersion);
console.log(config)

var consumer = new Kafka.KafkaConsumer({
	/*'debug': 'all',*/ 
    'api.version.request': 'true',
	'bootstrap.servers': config['bootstrap_servers'],
	'security.protocol' : 'sasl_ssl',
	'ssl.ca.location' : './ca-cert',
	'sasl.mechanisms' : 'PLAIN',
    'message.max.bytes': 32000,
    'fetch.max.bytes' : 32000,
    'fetch.message.max.bytes': 32000,
    'max.partition.fetch.bytes': 32000,
    'sasl.username' : config['sasl_plain_username'],
	'sasl.password' : config['sasl_plain_password'],
    'group.id' : config['consumer_id']
});


// Flowing mode:
consumer.connect();

consumer.on('ready', function() {
  console.log("connect ok");

  consumer.subscribe([config['topic_name']]);

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

consumer.on('error', function(error) {
	console.log("error:" + error);
});

consumer.on('event', function(event) {
        console.log("event:" + event);
});


