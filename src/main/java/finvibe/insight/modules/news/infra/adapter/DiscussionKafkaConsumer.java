package finvibe.insight.modules.news.infra.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.domain.News;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscussionKafkaConsumer {

    private final NewsRepository newsRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "discussion-events", groupId = "news-module-group")
    @Transactional
    public void handleDiscussionEvent(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            String type = json.get("type").asText();
            Long newsId = json.get("newsId").asLong();

            log.info("Received discussion event from Kafka: type={}, newsId={}", type, newsId);

            newsRepository.findById(newsId).ifPresent(news -> {
                if ("CREATED".equals(type)) {
                    news.incrementDiscussionCount();
                } else if ("DELETED".equals(type)) {
                    news.decrementDiscussionCount();
                }
                newsRepository.save(news);
                log.info("Updated discussion count for newsId={}: {}", newsId, news.getDiscussionCount());
            });
        } catch (Exception e) {
            log.error("Failed to process discussion event: {}", message, e);
        }
    }
}
