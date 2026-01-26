package finvibe.insight.modules.news.dto;

import finvibe.insight.modules.news.domain.Discussion;
import finvibe.insight.modules.news.domain.DiscussionComment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscussionDto {

    @Getter
    public static class Response {
        private final Long id;
        private final UUID userId;
        private final String content;
        private final Long newsId;
        private final long likeCount;
        private final List<CommentResponse> comments;
        private final LocalDateTime createdAt;

        public Response(Discussion discussion, long likeCount, List<CommentResponse> comments) {
            this.id = discussion.getId();
            this.userId = discussion.getUserId();
            this.content = discussion.getContent();
            this.newsId = discussion.getNews() != null ? discussion.getNews().getId() : null;
            this.likeCount = likeCount;
            this.comments = comments;
            this.createdAt = discussion.getCreatedAt();
        }
    }

    @Getter
    public static class CommentResponse {
        private final Long id;
        private final UUID userId;
        private final String content;
        private final LocalDateTime createdAt;

        public CommentResponse(DiscussionComment comment) {
            this.id = comment.getId();
            this.userId = comment.getUserId();
            this.content = comment.getContent();
            this.createdAt = comment.getCreatedAt();
        }
    }
}
