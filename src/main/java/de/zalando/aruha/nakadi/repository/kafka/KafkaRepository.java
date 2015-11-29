package de.zalando.aruha.nakadi.repository.kafka;

import java.io.IOException;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.ProducerRecord;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import de.zalando.aruha.nakadi.domain.Topic;
import de.zalando.aruha.nakadi.repository.TopicRepository;
import de.zalando.aruha.nakadi.repository.zookeeper.ZooKeeperFactory;

@Component
public class KafkaRepository implements TopicRepository {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaRepository.class);

    @Autowired
    private KafkaClientFactory factory;
    @Autowired
    private ZooKeeperFactory zkFactory;

    @Override
    public List<Topic> listTopics() {
        LOG.info("list topics");
        try {
            final ZooKeeper zk = zkFactory.get();
            final List<Topic> children = zk.getChildren("/brokers/topics", false).stream().map(s -> new Topic(s)).collect(Collectors.toList());
            LOG.info("topics: {}", children);
            return children;
        } catch (KeeperException | InterruptedException | IOException | NullPointerException e) {
            LOG.error("Failed to list topics", e);
        }

        return null;
    }

    @Override
    public void postMessage(final String topicId, final String partitionId, final String v) {
        LOG.info("%s %s %s", topicId, partitionId, v);

        final ProducerRecord record = new ProducerRecord<String, String>(topicId, partitionId, v);
        factory.createProducer().send(record);
    }
}
