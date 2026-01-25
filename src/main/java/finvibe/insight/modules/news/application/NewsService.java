package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.out.NewsCrawler;
import finvibe.insight.modules.news.application.port.out.NewsSummarizer;
import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.application.port.out.NewsCommentRepository;
import finvibe.insight.modules.news.application.port.out.NewsLikeRepository;
import finvibe.insight.modules.news.application.port.out.NewsCommentLikeRepository;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.domain.NewsComment;
import finvibe.insight.modules.news.domain.NewsLike;
import finvibe.insight.modules.news.domain.NewsCommentLike;
import finvibe.insight.modules.news.dto.NewsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;
    private final NewsCommentRepository newsCommentRepository;
    private final NewsLikeRepository newsLikeRepository;
    private final NewsCommentLikeRepository newsCommentLikeRepository;
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
     * 특정 뉴스를 상세 조회합니다 (좋아요, 댓글 목록 포함).
     */
    public NewsDto.DetailResponse findNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 뉴스입니다. id=" + id));

        long likeCount = newsLikeRepository.countByNewsId(id);
        long commentCount = newsCommentRepository.countByNewsId(id);
        List<NewsComment> comments = newsCommentRepository.findAllByNewsIdOrderByCreatedAtAsc(id);

        List<NewsDto.CommentResponse> commentResponses = convertToHierarchicalComments(comments);

        return new NewsDto.DetailResponse(news, likeCount, commentCount, commentResponses);
    }

    /**
     * 특정 댓글의 대댓글 목록을 조회합니다.
     */
    public List<NewsDto.CommentResponse> findRepliesByCommentId(Long commentId) {
        List<NewsComment> replies = newsCommentRepository.findAllByParentIdOrderByCreatedAtAsc(commentId);

        // 대댓글들도 각각의 하위 대댓글을 가질 수 있으므로 전체 목록에 대해 계층 구조 변환 수행
        return convertToHierarchicalComments(replies);
    }

    /**
     * 뉴스에 댓글을 작성합니다.
     */
    @Transactional
    public NewsDto.CommentResponse addComment(Long newsId, UUID userId, String content) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 뉴스입니다. id=" + newsId));

        NewsComment comment = NewsComment.create(news, userId, content);
        NewsComment saved = newsCommentRepository.save(comment);

        return new NewsDto.CommentResponse(saved, new ArrayList<>());
    }

    /**
     * 댓글에 대댓글을 작성합니다.
     */
    @Transactional
    public NewsDto.CommentResponse addReply(Long parentCommentId, UUID userId, String content) {
        NewsComment parent = newsCommentRepository.findById(parentCommentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다. id=" + parentCommentId));

        NewsComment reply = NewsComment.createReply(parent.getNews(), parent, userId, content);
        NewsComment saved = newsCommentRepository.save(reply);

        return new NewsDto.CommentResponse(saved, new ArrayList<>());
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
                                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 뉴스입니다. id=" + newsId));
                            newsLikeRepository.save(NewsLike.create(news, userId));
                        });
    }

    /**
     * 댓글 좋아요를 토글합니다.
     */
    @Transactional
    public void toggleCommentLike(Long commentId, UUID userId) {
        newsCommentLikeRepository.findByCommentIdAndUserId(commentId, userId)
                .ifPresentOrElse(
                        newsCommentLikeRepository::delete,
                        () -> {
                            NewsComment comment = newsCommentRepository.findById(commentId)
                                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다. id=" + commentId));
                            newsCommentLikeRepository.save(NewsCommentLike.create(comment, userId));
                        });
    }

    private List<NewsDto.CommentResponse> convertToHierarchicalComments(List<NewsComment> comments) {
        Map<Long, NewsDto.CommentResponse> responseMap = new HashMap<>();
        List<NewsDto.CommentResponse> roots = new ArrayList<>();

        // 1. 모든 댓글을 DTO로 변환하여 맵에 저장 (자식 목록은 일단 비어있음)
        for (NewsComment comment : comments) {
            responseMap.put(comment.getId(), new NewsDto.CommentResponse(comment, new ArrayList<>()));
        }

        // 2. 부모-자식 관계 연결
        for (NewsComment comment : comments) {
            NewsDto.CommentResponse currentDto = responseMap.get(comment.getId());
            if (comment.getParent() == null || !responseMap.containsKey(comment.getParent().getId())) {
                // 부모가 없거나, 부모가 현재 조회 대상 목록에 없는 경우 (현재 context에서의 root)
                roots.add(currentDto);
            } else {
                // 부모의 DTO를 찾아 자식 목록에 추가
                NewsDto.CommentResponse parentDto = responseMap.get(comment.getParent().getId());
                parentDto.getChildren().add(currentDto);
            }
        }

        return roots;
    }
}
