package finvibe.insight.modules.news.dto;

import finvibe.insight.modules.news.domain.EconomicSignal;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.domain.NewsComment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
        private final long commentCount;
        private final List<CommentResponse> comments;
        private final LocalDateTime createdAt;

        public DetailResponse(News news, long likeCount, long commentCount, List<CommentResponse> comments) {
            this.id = news.getId();
            this.title = news.getTitle();
            this.content = news.getContent();
            this.analysis = news.getAnalysis();
            this.category = news.getCategory();
            this.economicSignal = news.getEconomicSignal();
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.comments = comments;
            this.createdAt = news.getCreatedAt();
        }
    }

    @Getter
    public static class CommentResponse {
        private final Long id;
        private final UUID userId;
        private final String content;
        private final LocalDateTime createdAt;
        private List<CommentResponse> children;

        public CommentResponse(NewsComment comment, List<CommentResponse> children) {
            this.id = comment.getId();
            this.userId = comment.getUserId();
            this.content = comment.getContent();
            this.createdAt = comment.getCreatedAt();
            this.children = children;
        }
    }
}
