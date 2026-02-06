package finvibe.insight.modules.news.infra.adapter;

import finvibe.insight.modules.news.application.port.out.NewsCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class NaverNewsScraper implements NewsCrawler {

    private static final String NAVER_FINANCE_NEWS_URL = "https://finance.naver.com/news/mainnews.naver";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final Pattern PUBLISHED_AT_PATTERN = Pattern.compile("(\\d{4}\\.\\d{2}\\.\\d{2} \\d{2}:\\d{2})");
    private static final DateTimeFormatter PUBLISHED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    @Value("${news.crawler.max-items:20}")
    private int maxItems;

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

                // 상세 페이지 접속하여 본문/발행시간/신문사 가져오기
                NewsDetail detail = fetchDetail(detailUrl);

                if (detail.content() != null && !detail.content().isEmpty()) {
                    newsList.add(new RawNewsData(title, detail.content(), detail.publishedAt(), detail.provider()));
                }

                if (newsList.size() >= maxItems)
                    break;
            }

            log.info("Successfully fetched {} news items.", newsList.size());
        } catch (IOException e) {
            log.error("Failed to fetch news from Naver: {}", e.getMessage());
        }
        return newsList;
    }

    private NewsDetail fetchDetail(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .get();

            // 네이버 금융 뉴스 본문 영역 (id="content")
            Element contentElement = doc.selectFirst("#content");
            if (contentElement != null) {
                LocalDateTime publishedAt = extractPublishedAt(doc);
                String provider = extractProvider(doc);

                // 불필요한 태그 제거 (기자 정보, 저작권 등)
                contentElement.select(".link_news").remove();
                contentElement.select(".date").remove();
                return new NewsDetail(contentElement.text(), publishedAt, provider);
            }
        } catch (IOException e) {
            log.warn("Failed to fetch content from {}: {}", url, e.getMessage());
        }
        return new NewsDetail(null, null, null);
    }

    private LocalDateTime extractPublishedAt(Document doc) {
        List<String> selectors = List.of(
                ".article_info .date",
                ".article_info .article_time",
                ".media_end_head_info_datestamp_time",
                ".date");

        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element == null) {
                continue;
            }
            LocalDateTime parsed = parsePublishedAt(element.text());
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private LocalDateTime parsePublishedAt(String text) {
        Matcher matcher = PUBLISHED_AT_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return LocalDateTime.parse(matcher.group(1), PUBLISHED_AT_FORMATTER);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private String extractProvider(Document doc) {
        List<String> selectors = List.of(
                ".article_info .press",
                ".article_info .company",
                ".article_sponsor",
                ".media_end_head_top_logo img",
                ".source");

        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element == null) {
                continue;
            }
            String text = element.hasAttr("alt") ? element.attr("alt") : element.text();
            if (text != null && !text.isBlank()) {
                return text.trim();
            }
        }
        return null;
    }

    private record NewsDetail(String content, LocalDateTime publishedAt, String provider) {
    }
}
