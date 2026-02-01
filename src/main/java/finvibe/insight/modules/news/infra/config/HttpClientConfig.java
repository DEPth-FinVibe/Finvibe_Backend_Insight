package finvibe.insight.modules.news.infra.config;

import finvibe.insight.modules.news.infra.client.HttpDiscussionClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {

    @Value("${discussion.service.url:http://localhost:8080}")
    private String discussionServiceUrl;

    @Bean
    public HttpDiscussionClient httpDiscussionClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(discussionServiceUrl)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(HttpDiscussionClient.class);
    }
}
