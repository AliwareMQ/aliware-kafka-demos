/*
 * Copyright Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package streams;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Reducer;
import org.apache.kafka.streams.state.KeyValueStore;

//import sun.jvm.hotspot.runtime.Bytes;


public class BagDependencyChecker {

    static final String processedBagTopic = "processed_bags";
    static final String processedStreamTopic = "processed_streams";
    static final String readBagsTopic = "ready_bags";

    static public class ProcessedBag {
        public String file_path;
        public String bucket;
        public List<StreamFull> streams;
    }

    static public class ProcessedStream {
        public String id;
        public String bag_file_path;
        public String sensor_id;
        public Integer frame_num;
        public String topic;
        public Integer fps;
        public Long update_time;
    }

    static public class StreamFull extends ProcessedStream{
        public Long start_time;
        public Long end_time;
        public String city;
        public Double update_time;
        public String bag_category;
        public String topic;
        public String car;
        public Long collection_time;
        public String type;

    }

    static public class StreamCount {
        public String filePath;
        public Integer totalCount;
        public Integer processedCount;

        public StreamCount(String bagFilePath, int totalCount, int processedCount) {
            this.filePath = bagFilePath;
            this.totalCount= totalCount;
            this.processedCount = processedCount;
        }

        public boolean isFullyProcessed() {
            return processedCount >= totalCount;
        }
    }

    /**
     * The Streams application as a whole can be launched like any normal Java application that has a `main()` method.
     */
    public static void main(final String[] args) {
        final String bootstrapServers = args.length > 0 ? args[0] : "192.168.0.6:9092,192.168.0.5:9092,192.168.0.4:9092,192.168.0.3:9092";

        // Configure the Streams application.
        final Properties streamsConfiguration = getStreamsConfiguration(bootstrapServers);

        // Define the processing topology of the Streams application.
        final StreamsBuilder builder = new StreamsBuilder();
        dependencyCheckStream(builder);
        Topology topology = builder.build();
        // Print the topology
        System.out.println(topology.describe());
        final KafkaStreams streams = new KafkaStreams(topology, streamsConfiguration);

        // Always (and unconditionally) clean local state prior to starting the processing topology.
        // We opt for this unconditional call here because this will make it easier for you to play around with the example
        // when resetting the application for doing a re-run (via the Application Reset Tool,
        // http://docs.confluent.io/current/streams/developer-guide.html#application-reset-tool).
        //
        // The drawback of cleaning up local state prior is that your app must rebuilt its local state from scratch, which
        // will take time and will require reading all the state-relevant data from the Kafka cluster over the network.
        // Thus in a production scenario you typically do not want to clean up always as we do here but rather only when it
        // is truly needed, i.e., only under certain conditions (e.g., the presence of a command line flag for your app).
        // See `ApplicationResetExample.java` for a production-like example.
        streams.cleanUp();

        // Now run the processing topology via `start()` to begin processing its input data.
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close the Streams application.
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    /**
     * Configure the Streams application.
     * <p>
     * Various Kafka Streams related settings are defined here such as the location of the target Kafka cluster to use.
     * Additionally, you could also define Kafka Producer and Kafka Consumer settings when needed.
     *
     * @param bootstrapServers Kafka cluster address
     * @return Properties getStreamsConfiguration
     */
    static Properties getStreamsConfiguration(final String bootstrapServers) {
        final Properties streamsConfiguration = new Properties();
        // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
        // against which the application is run.
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "tbw");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "bag-dependency-checker-client");
        // Where to find Kafka broker(s).
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Specify default (de)serializers for record keys and for record values.
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        // Records should be flushed every 10 seconds. This is less than the default
        // in order to keep this example interactive.
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
        // For illustrative purposes we disable record caches.
        //streamsConfiguration.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        // Use a temporary directory for storing state, which will be automatically removed after the test.
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp");
        return streamsConfiguration;
    }

    static Serde<ProcessedBag> getProcessedBagsSerde() {
        Map<String, Object> serdeProps = new HashMap<>();
        final Serializer<ProcessedBag> processedBagSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", ProcessedBag.class);
        processedBagSerializer.configure(serdeProps, false);

        final Deserializer<ProcessedBag> processedBagDeserializer = new JsonPOJODeserializer<>();
        serdeProps.put("JsonPOJOClass", ProcessedBag.class);
        processedBagDeserializer.configure(serdeProps, false);
        final Serde<ProcessedBag> processedBagSerde = Serdes.serdeFrom(processedBagSerializer, processedBagDeserializer);
        return processedBagSerde;
    }


    static Serde<ProcessedStream> getProcessedStreamsSerde() {
        Map<String, Object> serdeProps = new HashMap<>();
        final Serializer<ProcessedStream> processedStreamSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", ProcessedStream.class);
        processedStreamSerializer.configure(serdeProps, false);

        final Deserializer<ProcessedStream> processedStreamDeserializer = new JsonPOJODeserializer<>();
        serdeProps.put("JsonPOJOClass", ProcessedStream.class);
        processedStreamDeserializer.configure(serdeProps, false);
        final Serde<ProcessedStream> processedStreamsSerde = Serdes.serdeFrom(processedStreamSerializer, processedStreamDeserializer);
        return processedStreamsSerde;
    }

    static Serde<HashSet> getHashsetSerde() {
        Map<String, Object> serdeProps = new HashMap<>();
        Serde<HashSet> hashSetSerde = SerdeFactory.createSerde(HashSet.class, serdeProps);
        return hashSetSerde;
    }

    static Serde<StreamCount> getStreamCountSerde() {
        Map<String, Object> serdeProps = new HashMap<>();
        Serde<StreamCount> streamCountSerde = SerdeFactory.createSerde(StreamCount.class, serdeProps);
        return streamCountSerde;
    }

    static int getValidStreams(List<StreamFull> streams) {
        int result = 0;
        for (StreamFull s: streams) {
            if (s.topic.contains("image_raw")) {
                result++;
                System.out.println("Found topic: " + s.topic);
            }
        }
        return result;
    }

    public static long SIX_HOURS = 6 * 3600 * 1000;
    static boolean isExpired(List<StreamFull> streams) {
        long currentMillis = System.currentTimeMillis() / 1000;
        if (!streams.isEmpty()) {
            System.out.println("Current millis:" + currentMillis + " Bag update time: " + streams.get(0).update_time);
            return streams.get(0).update_time < currentMillis - SIX_HOURS;
        } else {
            return true;
        }
    }

    static boolean isExpired(ProcessedStream stream) {
        long currentMillis = System.currentTimeMillis() / 1000;
        System.out.println("Current millis:" + currentMillis + " Stream update time: " + stream.update_time);
        return stream.update_time != null && stream.update_time < currentMillis - SIX_HOURS;

    }

    /**
     * Define the processing topology for Word Count.
     *
     * @param builder StreamsBuilder to use
     */
    static void dependencyCheckStream(final StreamsBuilder builder) {
        // Construct a `KStream` from the input topic "streams-plaintext-input", where message values
        // represent lines of text (for the sake of this example, we ignore whatever may be stored
        // in the message keys).  The default key and value serdes will be used.
        final Serde<ProcessedBag> processedBagSerde = BagDependencyChecker.getProcessedBagsSerde();
        final KStream<String, ProcessedBag> processedBagStream = builder.stream(processedBagTopic, Consumed.with(Serdes.String(), processedBagSerde)).filter((key, value) -> !isExpired(value.streams));

        processedBagStream.foreach(new ForeachAction<String, ProcessedBag>() {
            public void apply(String key, ProcessedBag value) {
                System.out.println("ProcessedBagStream:" + key + ": " + value);
            }
        });
        // final Pattern pattern = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);

//        final KTable<String, Integer> bagCountStream = processedBagStream.map((key, value) -> {
//            return new KeyValue<>(value.file_path, getValidStreams(value.streams));
//        }).groupByKey(Serialized.with(Serdes.String(), Serdes.Integer())).reduce(new Reducer<Integer>() {
//            public Integer apply(Integer value1, Integer value2) {
//                return value1 < value2 ? value1: value2;
//            }
//        }, Materialized.<String, Integer, KeyValueStore<Bytes, byte[]>>as("bag_count_store").with(Serdes.String(), Serdes.Integer()));


        final KTable<String, Integer> bagCountStream = processedBagStream.map((key, value) -> {
            return new KeyValue<>(value.file_path, getValidStreams(value.streams));
        }).groupBy((key, value) -> key, Grouped.with("bag-groupby", Serdes.String(), Serdes.Integer())).reduce(new Reducer<Integer>() {
            public Integer apply(Integer value1, Integer value2) {
                return value1 < value2 ? value1: value2;
            }
        }, Materialized.<String, Integer, KeyValueStore<Bytes, byte[]>>as("bag-count-store").withKeySerde(Serdes.String()).withValueSerde(Serdes.Integer()));

        bagCountStream.toStream().foreach(new ForeachAction<String, Integer>() {
            public void apply(String key, Integer value) {
                System.out.println("BagCountStream:" + key + ": " + value);
            }
        });


        final Serde<ProcessedStream> processedStreamSerde = BagDependencyChecker.getProcessedStreamsSerde();
        final KStream<String, String> streamCountKStream = builder.stream(processedStreamTopic, Consumed.with(Serdes.String(), processedStreamSerde)).filter((key, value) -> !isExpired(value)).map((key, value) -> {
            return new KeyValue<>(value.bag_file_path, value.id);
        });;

        //final KStream<String, String> streamCountKStream = processedStreamKStream

        final KTable<String, Integer> aggregatedStreamTable = streamCountKStream.groupBy((key, value) -> key, Grouped.with("stream-groupby", Serdes.String(), Serdes.String())).aggregate(
            () -> new HashSet(), /* initializer */
            (aggKey, value, aggValue) ->{
                aggValue.add(value);
                return aggValue;
            },
            Materialized.<String, HashSet, KeyValueStore<Bytes, byte[]>>as("stream-count-store").withKeySerde(Serdes.String()).withValueSerde(getHashsetSerde())).mapValues(v -> v.size()); /* serde for aggregate value */

        //final KTable<String, Integer> countedStreamTable = aggregatedStreamTable.mapValues(v -> v.size());
        aggregatedStreamTable.toStream().foreach(new ForeachAction<String, Integer>() {
            public void apply(String key, Integer value) {
                System.out.println("aggregatedStreamTable:" + key + ": " + value);
            }
        });

        final Serde<StreamCount> streamSerde = getStreamCountSerde();
        final KStream<String, StreamCount> fullyProcessedBags1 = bagCountStream.join(aggregatedStreamTable, (totalCount, processedCount) -> new StreamCount("", totalCount, processedCount)).toStream().filter((bag_file_path, streamCount) -> streamCount != null && streamCount.isFullyProcessed());

        fullyProcessedBags1.foreach(new ForeachAction<String, StreamCount>() {
            public void apply(String key, StreamCount value) {
                System.out.println("fullyProcessedBags1:" + key + ": " + value);
            }
        });
        final KStream<String, StreamCount> fullyProcessedBags  = fullyProcessedBags1.map((bagFilePath, streamCount)-> {
            streamCount.filePath = bagFilePath;
            return KeyValue.pair(bagFilePath, streamCount);
        });


        // Write the `KTable<String, Long>` to the output topic.
        fullyProcessedBags.to(readBagsTopic, Produced.with(Serdes.String(), streamSerde));
    }

}
