package finvibe.insight.modules.discussion.infra.adapter;

import finvibe.insight.modules.discussion.application.port.out.DiscussionEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscussionKafkaProducer implements DiscussionEventPort {

    private static final String TOPIC = "discussion-events";
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void publishCreated(Long newsId) {
        String message = String.format("{\"type\":\"CREATED\",\"newsId\":%d}", newsId);
        kafkaTemplate.send(TOPIC, message);
        log.info("Published DiscussionCreatedEvent to Kafka: newsId={}", newsId);
    }

    @Override
    public void publishDeleted(Long newsId) {
        String message = String.format("{\"type\":\"DELETED\",\"newsId\":%d}", newsId);
        kafkaTemplate.send(TOPIC, message);
        log.info("Published DiscussionDeletedEvent to Kafka: newsId={}", newsId);
    }
}
