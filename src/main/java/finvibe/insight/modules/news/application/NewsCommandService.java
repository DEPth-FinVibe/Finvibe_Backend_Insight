package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.in.NewsCommandUseCase;
import finvibe.insight.modules.news.application.port.out.*;
import finvibe.insight.modules.news.domain.*;
import finvibe.insight.modules.news.domain.error.NewsErrorCode;
import finvibe.insight.modules.news.dto.DiscussionDto;
import finvibe.insight.shared.error.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsCommandService implements NewsCommandUseCase {

    private final NewsRepository newsRepository;
    private final DiscussionRepository discussionRepository;
    private final DiscussionCommentRepository discussionCommentRepository;
    private final NewsLikeRepository newsLikeRepository;
    private final DiscussionLikeRepository discussionLikeRepository;
    private final NewsCrawler newsCrawler;
    private final NewsSummarizer newsSummarizer;

    @Override
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

    @Override
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

    @Override
    public DiscussionDto.CommentResponse addCommentToDiscussion(Long discussionId, UUID userId, String content) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new DomainException(NewsErrorCode.DISCUSSION_NOT_FOUND));

        DiscussionComment comment = DiscussionComment.create(discussion, userId, content);
        DiscussionComment saved = discussionCommentRepository.save(comment);

        return new DiscussionDto.CommentResponse(saved);
    }

    @Override
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

    @Override
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
