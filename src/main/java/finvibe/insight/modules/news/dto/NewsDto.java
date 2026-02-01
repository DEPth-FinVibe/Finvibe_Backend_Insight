package finvibe.insight.modules.news.dto;

import finvibe.insight.modules.news.domain.EconomicSignal;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.domain.NewsKeyword;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsDto {

    @Getter
    public static class Response {
        private final Long id;
        private final String title;
        private final EconomicSignal economicSignal;
        private final NewsKeyword keyword;
        private final LocalDateTime createdAt;

        public Response(News news) {
            this.id = news.getId();
            this.title = news.getTitle();
            this.economicSignal = news.getEconomicSignal();
            this.keyword = news.getKeyword();
            this.createdAt = news.getCreatedAt();
        }
    }

    @Getter
    public static class DetailResponse {
        private final Long id;
        private final String title;
        private final String content;
        private final String analysis;
        private final EconomicSignal economicSignal;
        private final NewsKeyword keyword;
        private final long likeCount;
        private final long discussionCount;
        private final LocalDateTime createdAt;

        public DetailResponse(News news, long likeCount, long discussionCount) {
            this.id = news.getId();
            this.title = news.getTitle();
            this.content = news.getContent();
            this.analysis = news.getAnalysis();
            this.economicSignal = news.getEconomicSignal();
            this.keyword = news.getKeyword();
            this.likeCount = likeCount;
            this.discussionCount = discussionCount;
            this.createdAt = news.getCreatedAt();
        }
    }
}
