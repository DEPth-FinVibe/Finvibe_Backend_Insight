package finvibe.insight;

import dev.langchain4j.model.chat.ChatModel;
import finvibe.insight.modules.news.infra.client.HttpDiscussionClient;
import finvibe.insight.modules.news.application.port.out.NewsCrawler;
import finvibe.insight.shared.dto.DiscussionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InsightApplicationTests {

	@MockitoBean
	private ChatModel chatModel;

	@MockitoBean
	private HttpDiscussionClient httpDiscussionClient;

	@MockitoBean
	private KafkaTemplate<String, DiscussionEvent> kafkaTemplate;

	@MockitoBean
	private NewsCrawler newsCrawler;

	@Test
	void contextLoads() {
	}

}
