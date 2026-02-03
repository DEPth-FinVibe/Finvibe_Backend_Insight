package finvibe.insight.modules.discussion.application.port.in;

import finvibe.insight.modules.discussion.dto.DiscussionDto;

import java.util.UUID;

public interface CommentCommandUseCase {
    DiscussionDto.CommentResponse addComment(Long discussionId, UUID userId, String content);

    DiscussionDto.CommentResponse updateComment(Long commentId, UUID userId, String content);

    void deleteComment(Long commentId, UUID userId);

    void toggleCommentLike(Long commentId, UUID userId);
}
