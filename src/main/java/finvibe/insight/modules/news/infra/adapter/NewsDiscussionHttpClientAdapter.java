package finvibe.insight.modules.news.infra.adapter;

import finvibe.insight.modules.discussion.dto.DiscussionDto;
import finvibe.insight.modules.discussion.dto.DiscussionSortType;
import finvibe.insight.modules.news.application.port.out.NewsDiscussionPort;
import finvibe.insight.modules.news.infra.client.HttpDiscussionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NewsDiscussionHttpClientAdapter implements NewsDiscussionPort {

    private final HttpDiscussionClient httpDiscussionClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    private CircuitBreaker discussionCountBreaker() {
        return circuitBreakerFactory.create("discussionServiceCount");
    }

    private CircuitBreaker discussionCountsBreaker() {
        return circuitBreakerFactory.create("discussionServiceCounts");
    }

    private CircuitBreaker discussionsBreaker() {
        return circuitBreakerFactory.create("discussionServiceList");
    }

    @Override
    public long getDiscussionCount(Long newsId) {
        return discussionCountBreaker().run(
                () -> {
                    Map<Long, Long> counts = httpDiscussionClient.getDiscussionCounts(List.of(newsId));
                    return counts.getOrDefault(newsId, 0L);
                },
                ex -> 0L
        );
    }

    /**
     * HTTP를 통해 외부 Discussion 서비스의 벌크 카운트 API를 호출합니다.
     * 분산 환경에서는 다른 노드의 엔드포인트를 호출하게 됩니다.
     */
    @Override
    public Map<Long, Long> getDiscussionCounts(List<Long> newsIds) {
        return discussionCountsBreaker().run(
                () -> httpDiscussionClient.getDiscussionCounts(newsIds),
                ex -> Map.of()
        );
    }

    @Override
    public List<DiscussionDto.Response> getDiscussions(Long newsId, DiscussionSortType sortType) {
        return discussionsBreaker().run(
                () -> httpDiscussionClient.getDiscussions(newsId, sortType),
                ex -> List.of()
        );
    }
}
