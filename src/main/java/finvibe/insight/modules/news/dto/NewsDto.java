package finvibe.insight.modules.news.dto;

import finvibe.insight.modules.news.domain.EconomicSignal;
import finvibe.insight.modules.news.domain.News;
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
        private final String category;
        private final EconomicSignal economicSignal;
        private final LocalDateTime createdAt;

        public Response(News news) {
            this.id = news.getId();
            this.title = news.getTitle();
            this.category = news.getCategory();
            this.economicSignal = news.getEconomicSignal();
            this.createdAt = news.getCreatedAt();
        }
    }

    @Getter
    public static class DetailResponse {
        private final Long id;
        private final String title;
        private final String content;
        private final String analysis;
        private final String category;
        private final EconomicSignal economicSignal;
        private final long likeCount;
        private final long discussionCount;
        private final List<DiscussionDto.Response> discussions;
        private final LocalDateTime createdAt;

        public DetailResponse(News news, long likeCount, long discussionCount,
                List<DiscussionDto.Response> discussions) {
            this.id = news.getId();
            this.title = news.getTitle();
            this.content = news.getContent();
            this.analysis = news.getAnalysis();
            this.category = news.getCategory();
            this.economicSignal = news.getEconomicSignal();
            this.likeCount = likeCount;
            this.discussionCount = discussionCount;
            this.discussions = discussions;
            this.createdAt = news.getCreatedAt();
        }
    }
}
