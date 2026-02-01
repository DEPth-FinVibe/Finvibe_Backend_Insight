package finvibe.insight.modules.news.infra.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;
import java.util.Map;

/**
 * Discussion 모듈의 HTTP API를 호출하기 위한 클라이언트
 * 실제 분산 환경에서는 다른 노드의 Discussion 서비스 엔드포인트를 호출합니다.
 */
@HttpExchange("/api/discussions")
public interface HttpDiscussionClient {

    /**
     * 여러 뉴스의 토론 수를 벌크로 조회합니다.
     * 
     * @param newsIds 뉴스 ID 목록
     * @return Map<newsId, count>
     */
    @GetExchange("/counts")
    Map<Long, Long> getDiscussionCounts(@RequestParam("newsIds") List<Long> newsIds);
}
