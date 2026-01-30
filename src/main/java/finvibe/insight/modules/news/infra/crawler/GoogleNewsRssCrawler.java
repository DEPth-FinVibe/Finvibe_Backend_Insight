package finvibe.insight.modules.news.infra.crawler;

import finvibe.insight.modules.news.application.port.out.NewsCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
public class GoogleNewsRssCrawler implements NewsCrawler {

    private static final String RSS_URL_TEMPLATE =
            "https://news.google.com/rss/search?q=%s&hl=ko&gl=KR&ceid=KR:ko";

    private static final List<String> KOREA_KEYWORDS = List.of(
            "코스피", "코스닥", "KOSPI", "KOSDAQ", "한국 증시", "국내 증시", "KRX",
            "삼성전자", "현대차", "LG에너지솔루션", "SK하이닉스", "원/달러", "환율");

    private static final List<String> US_KEYWORDS = List.of(
            "미국 증시", "미국 주식", "나스닥", "NASDAQ", "S&P", "S&P 500", "다우", "Dow",
            "연준", "Fed", "FOMC", "미국채", "CPI", "PCE", "실업률");

    private static final Set<String> TRUSTED_DOMAINS = Set.of(
            "yna.co.kr", "mk.co.kr", "hankyung.com", "sedaily.com", "edaily.co.kr",
            "newsis.com", "joongang.co.kr", "chosun.com", "donga.com",
            "reuters.com", "cnbc.com", "marketwatch.com", "investing.com",
            "bloomberg.com", "ft.com", "wsj.com");

    private static final List<String> QUERIES = List.of(
            "코스피 OR 코스닥 OR 국내 증시 OR KOSPI OR KOSDAQ",
            "미국 증시 OR 나스닥 OR S&P 500 OR 다우 OR 미국 주식");

    @Value("${news.crawler.max-items:20}")
    private int maxItems;

    @Override
    public List<RawNewsData> fetchLatestRawNews() {
        List<RawNewsData> results = new ArrayList<>();
        Set<String> seenTitles = new HashSet<>();
        Set<String> seenLinks = new HashSet<>();

        for (String query : QUERIES) {
            if (results.size() >= maxItems) {
                break;
            }
            results.addAll(fetchFromQuery(query, results.size(), seenTitles, seenLinks));
        }

        return results;
    }

    private List<RawNewsData> fetchFromQuery(String query, int currentSize,
            Set<String> seenTitles, Set<String> seenLinks) {
        List<RawNewsData> results = new ArrayList<>();
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format(RSS_URL_TEMPLATE, encodedQuery);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; FinvibeBot/1.0)")
                    .timeout((int) Duration.ofSeconds(10).toMillis())
                    .get();
            Elements items = doc.select("item");

            for (Element item : items) {
                if (currentSize + results.size() >= maxItems) {
                    break;
                }

                String title = sanitizeText(item.selectFirst("title"));
                String link = sanitizeText(item.selectFirst("link"));
                String description = extractDescription(item);
                String sourceUrl = extractSourceUrl(item);

                if (title.isBlank() || link.isBlank()) {
                    continue;
                }

                if (!isTrustedSource(sourceUrl)) {
                    continue;
                }

                if (!isRelevantNews(title, description)) {
                    continue;
                }

                if (!seenTitles.add(title) || !seenLinks.add(link)) {
                    continue;
                }

                String content = !description.isBlank() ? description : title;
                String category = resolveCategory(title, description);
                results.add(new RawNewsData(title, content, category));
            }
        } catch (IOException e) {
            log.warn("Failed to fetch Google News RSS for query: {}", query, e);
        }

        return results;
    }

    private static String extractDescription(Element item) {
        Element descriptionElement = item.selectFirst("description");
        if (descriptionElement == null) {
            return "";
        }
        String html = descriptionElement.text();
        return sanitizeText(Jsoup.parse(html).text());
    }

    private static String extractSourceUrl(Element item) {
        Element sourceElement = item.selectFirst("source");
        if (sourceElement == null) {
            return "";
        }
        return sourceElement.attr("url");
    }

    private static boolean isTrustedSource(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            return false;
        }
        try {
            String host = URI.create(sourceUrl).getHost();
            if (host == null) {
                return false;
            }
            String normalized = host.toLowerCase(Locale.ROOT);
            return TRUSTED_DOMAINS.stream().anyMatch(normalized::endsWith);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isRelevantNews(String title, String description) {
        String haystack = (title + " " + description).toLowerCase(Locale.ROOT);
        return containsAny(haystack, KOREA_KEYWORDS) || containsAny(haystack, US_KEYWORDS);
    }

    private static String resolveCategory(String title, String description) {
        String haystack = (title + " " + description).toLowerCase(Locale.ROOT);
        if (containsAny(haystack, KOREA_KEYWORDS)) {
            return "국장";
        }
        if (containsAny(haystack, US_KEYWORDS)) {
            return "미장";
        }
        return "기타";
    }

    private static boolean containsAny(String haystack, List<String> keywords) {
        for (String keyword : keywords) {
            if (haystack.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static String sanitizeText(Element element) {
        if (element == null) {
            return "";
        }
        return sanitizeText(element.text());
    }

    private static String sanitizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }
}
