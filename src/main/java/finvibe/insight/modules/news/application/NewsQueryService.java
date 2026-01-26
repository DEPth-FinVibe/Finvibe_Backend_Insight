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
    public List<DiscussionDto.Response> findRepliesByCommentId(Long commentId) {
        // 기존 비즈니스 로직: 특정 부모 댓글(여기선 토론의 댓글 개념이나 논리적 유사성 유지) 하위 조회
        // 현재 2단계 구조에서는 '특정 토론의 댓글 목록' 조회가 주를 이름.
        // 유스케이스 정의에 따라 특정 토론 하위 전체를 반환하거나 필요 시 로직 조정.
        // 우선 기존 인터페이스 모양 유지 (필요 시 DiscussionComment 관련 로직으로 구현 가능)
        return List.of();
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
