package finvibe.insight.modules.news.application.port.in;

import finvibe.insight.modules.news.dto.DiscussionDto;

import java.util.UUID;

public interface NewsCommandUseCase {
    void syncLatestNews();

    DiscussionDto.Response addDiscussion(Long newsId, UUID userId, String content);

    DiscussionDto.CommentResponse addCommentToDiscussion(Long discussionId, UUID userId, String content);

    void toggleNewsLike(Long newsId, UUID userId);

    void toggleDiscussionLike(Long discussionId, UUID userId);
}
