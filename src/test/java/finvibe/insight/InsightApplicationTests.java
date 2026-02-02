package finvibe.insight;

import dev.langchain4j.model.chat.ChatModel;
import finvibe.insight.modules.news.infra.client.HttpDiscussionClient;
import finvibe.insight.modules.news.application.port.out.NewsCrawler;
import finvibe.insight.shared.dto.DiscussionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InsightApplicationTests {

	@MockBean
	private ChatModel chatModel;

	@MockBean
	private HttpDiscussionClient httpDiscussionClient;

	@MockBean
	private KafkaTemplate<String, DiscussionEvent> kafkaTemplate;

	@MockBean
	private NewsCrawler newsCrawler;

	@Test
	void contextLoads() {
	}

}
