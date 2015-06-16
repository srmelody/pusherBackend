package com.rallydev.pusher;

import com.pusher.rest.Pusher;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class ChangeMessageProcessor {
    private final CryptoHolder cryptoHolder;
    private ExecutorService executor;
    private ConsumerConnector consumer;

    public ChangeMessageProcessor(CryptoHolder cryptoHolder) {
        this.cryptoHolder = cryptoHolder;
    }

    private static ConsumerConfig createConsumerConfig(String zk, String groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", zk);
        props.put("group.id", groupId);
        props.put("zookeeper.session.timeout.ms", "4000");
        props.put("zookeeper.sync.time.ms", "2000");
        props.put("auto.commit.interval.ms", "1000");
        props.put("rebalance.backoff.ms", "5000");
        return new ConsumerConfig(props);
    }

    // ./kafka-topics.sh  --zookeeper "bld-zookeeper-01:2181,bld-zookeeper-02:2181,bld-zookeeper-03:2181" --list
    public void subscribe(Pusher pusher) {                                                           //,bld-zookeeper-02.f4tech.com:2181,bld-zookeeper-03.f4tech.com:2181"
        ConsumerConfig config = createConsumerConfig("bld-zookeeper-01.f4tech.com/kafka8", "sean-test");
        consumer = Consumer.createJavaConsumerConnector(config);

        String topic = "pusher-test";
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        int numThreads = 4;
        topicCountMap.put(topic, new Integer(numThreads));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        System.out.println("streams.size = " + streams.size());

        // now launch all the threads
        //
        executor = Executors.newFixedThreadPool(numThreads);

        // now create an object to consume the messages
        //
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new PusherConsumer(stream, threadNumber, pusher, cryptoHolder));
            threadNumber++;
        }

    }

}
