package finvibe.insight.boot.config;

import finvibe.insight.modules.news.infra.client.HttpDiscussionClient;
import finvibe.insight.modules.news.infra.client.HttpMarketClient;
import finvibe.insight.modules.news.infra.client.HttpMarketCategoryClient;
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

    @Value("${market.service.url:http://investment}")
    private String marketServiceUrl;

    @Value("${market.category.service.url:http://investment}")
    private String marketCategoryServiceUrl;

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

    @Bean
    public HttpMarketClient httpMarketClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(marketServiceUrl)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(HttpMarketClient.class);
    }

    @Bean
    public HttpMarketCategoryClient httpMarketCategoryClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(marketCategoryServiceUrl)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(HttpMarketCategoryClient.class);
    }
}
