package com.alibaba.migration.cmd;

import com.aliyuncs.IAcsClient;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.utils.Bytes;

@Parameters(commandDescription = "ConsumerGroup migration from Aliyun")
public class ConsumerGroupMigrationFromTopic extends AbstractMigration {

    @Parameter(names = "--propertiesPath", description = "path of kafka.properties", required = true)
    protected String path;

    @Override public String cmdName() {
        return "ConsumerGroupMigrationFromTopic";
    }

    @Override public Cmd newCmd() {
        return new ConsumerGroupMigrationFromTopic();
    }

    @Override public void run() {

        KafkaConsumer kafkaConsumer = initKafkaConsumer();
        Set<String> consumerGroups = new HashSet<>();

        // 循环消费消息，从__consumer_offsets获取有效consumer group
        boolean pollHasData = false;
        while (true) {
            try {
                ConsumerRecords<ByteBuffer, ByteBuffer> records = kafkaConsumer.poll(1000);

                // 获取过数据，并且本次获取不到数据，以此判断已经读到末尾了。
                if (pollHasData && records.isEmpty()) {
                    break;
                }
                for (ConsumerRecord<ByteBuffer, ByteBuffer> record : records) {
                    pollHasData = true;
                    ByteBuffer byteBuffer = record.key();
                    short version = byteBuffer.getShort();
                    // version == 2 表示是group metadata消息
                    if (version != 2) {
                        continue;
                    }
                    // 只处理group metadata消息
                    short length = byteBuffer.getShort();
                    byte[] bytes = new byte[length];
                    byteBuffer.get(bytes);
                    String groupName = new String(bytes, "UTF-8");
                    if (null == record.value()) {
                        consumerGroups.remove(groupName);
                    } else {
                        consumerGroups.add(groupName);
                    }
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (Throwable ignore) {

                }
                logger.error("ConsumerGroupMigrationFromTopic process message exception.", e);
            }
        }

        if (commit) {

            IAcsClient destIAcsClient = buildAcsClient(this.destAk, this.destSk, this.destRegionId, this.destInstanceId);
            createConsumerGroupInYunKafka(destIAcsClient, destRegionId, destInstanceId, new ArrayList<>(consumerGroups));
        } else {

            logger.info("Will create consumer groups:{}", consumerGroups);
        }
    }

    private KafkaConsumer initKafkaConsumer() {

        //设置sasl文件的路径
        JavaKafkaConfigurer.configureSasl(path);

        //加载kafka.properties
        Properties kafkaProperties = JavaKafkaConfigurer.getKafkaProperties(path);

        Properties props = new Properties();
        //设置接入点，请通过控制台获取对应Topic的接入点
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getProperty("bootstrap.servers"));

        //truststore路径
        if (kafkaProperties.getProperty("ssl.truststore.location") != null) {
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaProperties.getProperty("ssl.truststore.location"));
        }
        //根证书store的密码
        if (kafkaProperties.getProperty("ssl.truststore.password") != null) {
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaProperties.getProperty("ssl.truststore.password"));
        }

        //接入协议
        if (kafkaProperties.getProperty("security.protocol") != null) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, kafkaProperties.getProperty("security.protocol"));
        }
        //鉴权方式
        if (kafkaProperties.getProperty("sasl.mechanism") != null) {
            props.put(SaslConfigs.SASL_MECHANISM, kafkaProperties.getProperty("sasl.mechanism"));
        }
        //两次poll之间的最大允许间隔
        //可更加实际拉去数据和客户的版本等设置此值，默认30s
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        //每次poll的最大数量
        //注意该值不要改得太大，如果poll太多数据，而不能在下次poll之前消费完，则会触发一次负载均衡，产生卡顿
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 30);
        //消息的反序列化方式
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteBufferDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteBufferDeserializer");
        //当前消费实例所属的消费组，请在控制台申请之后填写
        //属于同一个组的消费实例，会负载消费消息
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getProperty("group.id"));
        //构造消息对象，也即生成一个消费实例
        KafkaConsumer<Bytes, Bytes> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(props);
        //设置消费组订阅的Topic，可以订阅多个
        //如果GROUP_ID_CONFIG是一样，则订阅的Topic也建议设置成一样
        List<String> subscribedTopics = new ArrayList<String>();
        //如果需要订阅多个Topic，则在这里add进去即可
        //每个Topic需要先在控制台进行创建
        subscribedTopics.add("__consumer_offsets");
        consumer.subscribe(subscribedTopics);

        return consumer;
    }
}
