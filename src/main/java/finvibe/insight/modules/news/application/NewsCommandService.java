package finvibe.insight.modules.news.application;

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
