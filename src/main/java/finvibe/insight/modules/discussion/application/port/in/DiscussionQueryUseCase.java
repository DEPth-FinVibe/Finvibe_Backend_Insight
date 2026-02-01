package finvibe.insight.modules.discussion.application.port.in;

import finvibe.insight.modules.discussion.dto.DiscussionDto;
import finvibe.insight.modules.discussion.dto.DiscussionSortType;

import java.util.List;

public interface DiscussionQueryUseCase {
    long countByNewsId(Long newsId);

    java.util.Map<Long, Long> countByNewsIds(java.util.List<Long> newsIds);

    List<DiscussionDto.Response> findAllByNewsId(Long newsId);

    List<DiscussionDto.Response> findAll(DiscussionSortType sortType);

    List<DiscussionDto.CommentResponse> findCommentsByDiscussionId(Long discussionId);
}
