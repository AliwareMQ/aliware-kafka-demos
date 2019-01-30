package com.aliyun.openservices.kafka.ons;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 */
public class MainDemo {
    public static void main(String[] args) {
        Options options = new Options();
        options.addRequiredOption("r", "producerOrConsumer", true, "run with producer or consumer");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String type = cmd.getOptionValue("r");
        if ("producer".equals(type)) {
            KafkaProducerDemo producer = new KafkaProducerDemo();
            producer.doSend();

        } else if ("consumer".equals(type)) {
            KafkaConsumerDemo consumer = new KafkaConsumerDemo();
            consumer.doConsumer();

        } else {
            throw new IllegalArgumentException("the type is illegal, please check !!");
        }
    }
}
