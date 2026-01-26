package finvibe.insight.modules.discussion.application.port.in;

import finvibe.insight.modules.discussion.dto.DiscussionDto;

import java.util.List;

public interface DiscussionQueryUseCase {
    long countByNewsId(Long newsId);

    List<DiscussionDto.Response> findAllByNewsId(Long newsId);

    List<DiscussionDto.CommentResponse> findCommentsByDiscussionId(Long discussionId);
}
