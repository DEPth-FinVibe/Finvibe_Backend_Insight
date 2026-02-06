package finvibe.insight.modules.news.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsCrawler {
    List<RawNewsData> fetchLatestRawNews();

    record RawNewsData(String title, String content, LocalDateTime publishedAt, String provider) {
    }
}
