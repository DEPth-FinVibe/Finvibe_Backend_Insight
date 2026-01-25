package finvibe.insight.modules.news.application.port.out;

import java.util.List;

public interface NewsCrawler {
    List<RawNewsData> crawlLatestNews();

    record RawNewsData(String title, String content, String category) {
    }
}
