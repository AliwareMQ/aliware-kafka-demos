package com.aliyun.openservices.alikafka;

import java.util.Properties;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

/**
 * Consume Kafka Topic Message as source of Flink
 */
public class FlinkKafkaConsumerDemo {
	public static void main(String[] args) throws Exception {

		// parse input arguments
		final ParameterTool parameterTool = ParameterTool.fromArgs(args);

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.getConfig().disableSysoutLogging();
		env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000));
		env.enableCheckpointing(5000); // 5秒一次checkpoint
		env.getConfig().setGlobalJobParameters(parameterTool); // make parameters available in the web interface

		Properties appEnv = AliKafkaConfigurer.getKafkaProperties();

		Properties pros = parameterTool.getProperties();
		pros.putAll(appEnv);
		pros.putAll(AliKafkaConfigurer.saslSSLConfig());

		DataStreamSource<String> messageStream = env
				.addSource(new FlinkKafkaConsumer010(pros.getProperty("topic"), new SimpleStringSchema(), pros));
		messageStream.setParallelism(1);

		messageStream.addSink(new SinkFunction<String>() {

			private static final long serialVersionUID = -7208093225178799588L;

			public void invoke(String value) throws Exception {
				System.out.println(value);
			}

		});

		env.execute("Read from Kafka example");
	}
}
