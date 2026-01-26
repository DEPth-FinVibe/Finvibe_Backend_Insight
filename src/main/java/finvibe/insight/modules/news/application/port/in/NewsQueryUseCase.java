package finvibe.insight.modules.news.application.port.in;

import finvibe.insight.modules.news.dto.DiscussionDto;
import finvibe.insight.modules.news.dto.NewsDto;

import java.util.List;

public interface NewsQueryUseCase {
    List<NewsDto.Response> findAllNewsSummary();

    NewsDto.DetailResponse findNewsById(Long id);

    List<DiscussionDto.CommentResponse> findCommentsByDiscussionId(Long discussionId);
}
