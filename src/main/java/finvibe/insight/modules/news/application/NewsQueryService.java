package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.in.NewsQueryUseCase;
import finvibe.insight.modules.news.application.port.out.*;
import finvibe.insight.modules.news.domain.Discussion;
import finvibe.insight.modules.news.domain.DiscussionComment;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.domain.error.NewsErrorCode;
import finvibe.insight.modules.news.dto.DiscussionDto;
import finvibe.insight.modules.news.dto.NewsDto;
import finvibe.insight.shared.error.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsQueryService implements NewsQueryUseCase {

    private final NewsRepository newsRepository;
    private final DiscussionRepository discussionRepository;
    private final DiscussionCommentRepository discussionCommentRepository;
    private final NewsLikeRepository newsLikeRepository;
    private final DiscussionLikeRepository discussionLikeRepository;

    @Override
    public List<NewsDto.Response> findAllNewsSummary() {
        return newsRepository.findAll().stream()
                .map(NewsDto.Response::new)
                .toList();
    }

    @Override
    public NewsDto.DetailResponse findNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new DomainException(NewsErrorCode.NEWS_NOT_FOUND));

        long likeCount = newsLikeRepository.countByNewsId(id);
        long discussionCount = discussionRepository.countByNewsId(id);
        List<Discussion> discussions = discussionRepository.findAllByNewsIdOrderByCreatedAtAsc(id);

        List<DiscussionDto.Response> discussionResponses = discussions.stream()
                .map(this::mapToDiscussionResponse)
                .toList();

        return new NewsDto.DetailResponse(news, likeCount, discussionCount, discussionResponses);
    }

    @Override
    public List<DiscussionDto.CommentResponse> findCommentsByDiscussionId(Long discussionId) {
        return discussionCommentRepository.findAllByDiscussionIdOrderByCreatedAtAsc(discussionId).stream()
                .map(DiscussionDto.CommentResponse::new)
                .toList();
    }

    private DiscussionDto.Response mapToDiscussionResponse(Discussion discussion) {
        long likeCount = discussionLikeRepository.countByDiscussionId(discussion.getId());
        List<DiscussionComment> comments = discussionCommentRepository
                .findAllByDiscussionIdOrderByCreatedAtAsc(discussion.getId());

        List<DiscussionDto.CommentResponse> commentDtos = comments.stream()
                .map(DiscussionDto.CommentResponse::new)
                .toList();

        return new DiscussionDto.Response(discussion, likeCount, commentDtos);
    }
}
