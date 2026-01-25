package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.out.NewsCrawler;
import finvibe.insight.modules.news.application.port.out.NewsSummarizer;
import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.dto.NewsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;
    private final NewsCrawler newsCrawler;
    private final NewsSummarizer newsSummarizer;

    /**
     * 뉴스를 크롤링하고 AI로 요약하여 저장합니다.
     */
    @Transactional
    public void crawlAndCreateNews() {
        List<NewsCrawler.RawNewsData> rawDataList = newsCrawler.crawlLatestNews();

        for (NewsCrawler.RawNewsData rawData : rawDataList) {
            NewsSummarizer.AnalysisResult analysis = newsSummarizer.summarize(rawData.content());

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
     * 뉴스 목록을 조회합니다.
     */
    public List<NewsDto.Response> getNewsList() {
        return newsRepository.findAll().stream()
                .map(NewsDto.Response::new)
                .toList();
    }

    /**
     * 특정 뉴스의 상세 내용을 조회합니다.
     */
    public NewsDto.DetailResponse getNewsDetail(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 뉴스입니다. id=" + id));
        return new NewsDto.DetailResponse(news);
    }
}
