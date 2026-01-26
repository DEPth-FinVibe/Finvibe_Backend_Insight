package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.out.*;
import finvibe.insight.modules.news.domain.*;
import finvibe.insight.modules.news.domain.error.NewsErrorCode;
import finvibe.insight.modules.news.dto.DiscussionDto;
import finvibe.insight.modules.news.dto.NewsDto;
import finvibe.insight.shared.error.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;
    private final DiscussionRepository discussionRepository;
    private final DiscussionCommentRepository discussionCommentRepository;
    private final NewsLikeRepository newsLikeRepository;
    private final DiscussionLikeRepository discussionLikeRepository;
    private final NewsCrawler newsCrawler;
    private final NewsSummarizer newsSummarizer;

    /**
     * 최신 뉴스를 수집하여 분석 후 저장합니다.
     */
    @Transactional
    public void syncLatestNews() {
        List<NewsCrawler.RawNewsData> rawDataList = newsCrawler.fetchLatestRawNews();

        for (NewsCrawler.RawNewsData rawData : rawDataList) {
            NewsSummarizer.AnalysisResult analysis = newsSummarizer.analyzeAndSummarize(rawData.content());

            News news = News.create(
                    rawData.title(),
                    rawData.content(),
                    rawData.category(),
                    analysis.summary(),
                    analysis.signal());

            newsRepository.save(news);
        }
    }

    /**
     * 뉴스 전체 목록을 요약 형태로 조회합니다.
     */
    public List<NewsDto.Response> findAllNewsSummary() {
        return newsRepository.findAll().stream()
                .map(NewsDto.Response::new)
                .toList();
    }

    /**
     * 특정 뉴스를 상세 조회합니다 (좋아요, 토론 목록 포함).
     */
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

    /**
     * 토론을 작성합니다 (뉴스 연관 관계는 선택적).
     */
    @Transactional
    public DiscussionDto.Response addDiscussion(Long newsId, UUID userId, String content) {
        News news = null;
        if (newsId != null) {
            news = newsRepository.findById(newsId)
                    .orElseThrow(() -> new DomainException(NewsErrorCode.NEWS_NOT_FOUND));
        }

        Discussion discussion = Discussion.create(news, userId, content);
        Discussion saved = discussionRepository.save(discussion);

        return mapToDiscussionResponse(saved);
    }

    /**
     * 토론에 댓글을 작성합니다.
     */
    @Transactional
    public DiscussionDto.CommentResponse addCommentToDiscussion(Long discussionId, UUID userId, String content) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new DomainException(NewsErrorCode.DISCUSSION_NOT_FOUND));

        DiscussionComment comment = DiscussionComment.create(discussion, userId, content);
        DiscussionComment saved = discussionCommentRepository.save(comment);

        return new DiscussionDto.CommentResponse(saved);
    }

    /**
     * 뉴스 좋아요를 토글합니다.
     */
    @Transactional
    public void toggleNewsLike(Long newsId, UUID userId) {
        newsLikeRepository.findByNewsIdAndUserId(newsId, userId)
                .ifPresentOrElse(
                        newsLikeRepository::delete,
                        () -> {
                            News news = newsRepository.findById(newsId)
                                    .orElseThrow(() -> new DomainException(NewsErrorCode.NEWS_NOT_FOUND));
                            newsLikeRepository.save(NewsLike.create(news, userId));
                        });
    }

    /**
     * 토론 좋아요를 토글합니다.
     */
    @Transactional
    public void toggleDiscussionLike(Long discussionId, UUID userId) {
        discussionLikeRepository.findByDiscussionIdAndUserId(discussionId, userId)
                .ifPresentOrElse(
                        discussionLikeRepository::delete,
                        () -> {
                            Discussion discussion = discussionRepository.findById(discussionId)
                                    .orElseThrow(() -> new DomainException(NewsErrorCode.DISCUSSION_NOT_FOUND));
                            discussionLikeRepository.save(DiscussionLike.create(discussion, userId));
                        });
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
