package finvibe.insight.modules.discussion.application.port.in;

import finvibe.insight.modules.discussion.dto.DiscussionDto;

import java.util.UUID;

public interface DiscussionCommandUseCase {
    DiscussionDto.Response addDiscussion(Long newsId, UUID userId, String content);

    DiscussionDto.Response updateDiscussion(Long discussionId, UUID userId, String content);

    void deleteDiscussion(Long discussionId, UUID userId);

    DiscussionDto.CommentResponse addComment(Long discussionId, UUID userId, String content);

    DiscussionDto.CommentResponse updateComment(Long commentId, UUID userId, String content);

    void deleteComment(Long commentId, UUID userId);

    void toggleDiscussionLike(Long discussionId, UUID userId);

    void toggleCommentLike(Long commentId, UUID userId);
}
