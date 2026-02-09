package finvibe.insight.modules.discussion.dto;

import finvibe.insight.modules.discussion.domain.DiscussionComment;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscussionCommentDto {

    public record CreateRequest(
            @NotBlank(message = "content는 비어 있을 수 없습니다.")
            String content) {
    }

    @Getter
    @NoArgsConstructor
    public static class CreateRequest {
        private String content;
    }

    @Getter
    public static class Response {
        private final Long id;
        private final UUID userId;
        private final String content;
        private final boolean isEdited;
        private final long likeCount;
        private final LocalDateTime createdAt;

        public Response(DiscussionComment comment, long likeCount) {
            this.id = comment.getId();
            this.userId = comment.getUserId();
            this.content = comment.getContent();
            this.isEdited = comment.isEdited();
            this.likeCount = likeCount; // 좋아요 수 추가
            this.createdAt = comment.getCreatedAt();
        }
    }
}
