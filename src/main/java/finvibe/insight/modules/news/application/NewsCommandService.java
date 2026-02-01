package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.out.NewsDiscussionCountPort;
import finvibe.insight.modules.news.application.port.in.NewsCommandUseCase;
import finvibe.insight.modules.news.application.port.out.NewsCrawler;
import finvibe.insight.modules.news.application.port.out.NewsLikeRepository;
import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.application.port.out.NewsSummarizer;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.domain.NewsLike;
import finvibe.insight.modules.news.domain.error.NewsErrorCode;
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
    private final NewsLikeRepository newsLikeRepository;
    private final NewsCrawler newsCrawler;
    private final NewsSummarizer newsSummarizer;
    private final NewsDiscussionCountPort newsDiscussionCountPort;

    @Override
    public void syncLatestNews() {
        List<NewsCrawler.RawNewsData> rawDataList = newsCrawler.fetchLatestRawNews();

        for (NewsCrawler.RawNewsData rawData : rawDataList) {
            if (newsRepository.existsByTitle(rawData.title())) {
                continue;
            }

            String analysisInput = "제목: " + rawData.title() + "\n요약: " + rawData.content();
            NewsSummarizer.AnalysisResult analysis = newsSummarizer.analyzeAndSummarize(analysisInput);

            News news = News.create(
                    rawData.title(),
                    rawData.content(),
                    analysis.summary(),
                    analysis.signal(),
                    analysis.keyword());

            newsRepository.save(news);
        }
    }

    @Override
    public void syncAllDiscussionCounts() {
        List<News> allNews = newsRepository.findAll();
        List<Long> newsIds = allNews.stream().map(News::getId).toList();

        // 벌크로 토론 수 조회 (네트워크 호출 최소화)
        java.util.Map<Long, Long> countsMap = newsDiscussionCountPort.getDiscussionCounts(newsIds);

        for (News news : allNews) {
            long currentCount = countsMap.getOrDefault(news.getId(), 0L);

            if (news.getDiscussionCount() != currentCount) {
                news.syncDiscussionCount(currentCount);
                newsRepository.save(news);
            }
        }
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
}
