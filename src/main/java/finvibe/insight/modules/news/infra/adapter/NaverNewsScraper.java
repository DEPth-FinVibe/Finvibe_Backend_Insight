package finvibe.insight.modules.news.infra.adapter;

import finvibe.insight.modules.news.application.port.out.NewsCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class NaverNewsScraper implements NewsCrawler {

    private static final String NAVER_FINANCE_NEWS_URL = "https://finance.naver.com/news/mainnews.naver";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    public List<RawNewsData> fetchLatestRawNews() {
        List<RawNewsData> newsList = new ArrayList<>();
        try {
            log.info("Fetching latest news from Naver Finance...");
            Document doc = Jsoup.connect(NAVER_FINANCE_NEWS_URL)
                    .userAgent(USER_AGENT)
                    .get();

            // 주요 뉴스 목록 추출 (네이버 금융 메인 뉴스 구조 기준)
            Elements newsElements = doc.select(".mainNewsList .block1");

            for (Element element : newsElements) {
                Element linkElement = element.selectFirst("dl dt:not(.photo) a");
                if (linkElement == null)
                    continue;

                String title = linkElement.text();
                String detailUrl = "https://finance.naver.com" + linkElement.attr("href");

                // 상세 페이지 접속하여 본문 가져오기
                String content = fetchContent(detailUrl);

                if (content != null && !content.isEmpty()) {
                    newsList.add(new RawNewsData(title, content));
                }

                // 너무 많은 뉴스를 가져오지 않도록 제한 (테스트용)
                if (newsList.size() >= 5)
                    break;
            }

            log.info("Successfully fetched {} news items.", newsList.size());
        } catch (IOException e) {
            log.error("Failed to fetch news from Naver: {}", e.getMessage());
        }
        return newsList;
    }

    private String fetchContent(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .get();

            // 네이버 금융 뉴스 본문 영역 (id="content")
            Element contentElement = doc.selectFirst("#content");
            if (contentElement != null) {
                // 불필요한 태그 제거 (기자 정보, 저작권 등)
                contentElement.select(".link_news").remove();
                contentElement.select(".date").remove();
                return contentElement.text();
            }
        } catch (IOException e) {
            log.warn("Failed to fetch content from {}: {}", url, e.getMessage());
        }
        return null;
    }
}
