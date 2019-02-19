const co = require('co')
const Kafka = require('node-rdkafka');
const config = require('./setting');
console.log(Kafka.features);
console.log(Kafka.librdkafkaVersion);

//该函数每次都会创建producer进行发送，性能可能较差，但是使用简答，仅供参考
var produceMessages = function(msgs) {
  return new Promise(function(resolve, reject) {
    let msgsCount = 1;
    if(Array.isArray(msgs)){
      msgsCount = msgs.length;
    };
    var producer = new Kafka.Producer({
        /*'debug': 'all', */
        'api.version.request': 'true',
        'bootstrap.servers': config['bootstrap_servers'],
        'dr_cb': true,
        'dr_msg_cb': true,
        'security.protocol' : 'sasl_ssl',
        'ssl.ca.location' : './ca-cert',
        'sasl.mechanisms' : 'PLAIN',
        'sasl.username' : config['sasl_plain_username'],
        'sasl.password' : config['sasl_plain_password']
    });

    // Poll for events every 100 ms
    producer.setPollInterval(100);

    // Connect to the broker manually
    producer.connect();

    var produceSingleMessage = function(singleMsg){
        try {
        producer.produce(
          // Topic to send the message to
          config['topic_name'],
          // optionally we can manually specify a partition for the message
          // this defaults to -1 - which will use librdkafka's default partitioner (consistent random for keyed messages, random for unkeyed messages)
          null,
          // Message to send. Must be a buffer
          new Buffer(singleMsg),
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
        reject(producer);
      }
    }
    // Wait for the ready event before proceeding
    producer.on('ready', function() {
      console.log("connect ok")
      //连接成功后，尝试发一条或者n条消息
      if(Array.isArray(msgs)){
        msgs.forEach(function(singleMsg){
            produceSingleMessage(singleMsg);
        });
      } else{
        produceSingleMessage(msgs);
      }

    });

    producer.on("disconnected", function() {
      reject(producer);
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
      //n条全部发送完，就关闭连接
      producer.disconnect();
      msgsCount--;
      if(!msgsCount){
        resolve(producer);
      }
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
  })
}


co.wrap(function* () {
  var p1 = yield produceMessages('Hello1')
  var p2 = yield produceMessages('Hello2')
  var p3 = yield produceMessages(['Hello3', 'Hello4', 'Hello5'])
})()
