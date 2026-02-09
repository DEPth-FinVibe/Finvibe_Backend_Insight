package finvibe.insight.modules.news.dto;

import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.domain.ThemeDaily;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThemeDto {

    @Getter
    public static class SummaryResponse {
        private final Long categoryId;
        private final String categoryName;
        private final java.math.BigDecimal averageChangePct;

        public SummaryResponse(ThemeDaily themeDaily, java.math.BigDecimal averageChangePct) {
            this.categoryId = themeDaily.getCategory().getId();
            this.categoryName = themeDaily.getCategory().getName();
            this.averageChangePct = averageChangePct;
        }
    }

    @Getter
    public static class DetailResponse {
        private final Long categoryId;
        private final String categoryName;
        private final List<NewsSummary> news;

        public DetailResponse(ThemeDaily themeDaily, List<NewsSummary> news) {
            this.categoryId = themeDaily.getCategory().getId();
            this.categoryName = themeDaily.getCategory().getName();
            this.news = news;
        }
    }

    @Getter
    public static class NewsSummary {
        private final String title;
        private final LocalDateTime publishedAt;
        private final String provider;

        public NewsSummary(News news) {
            this.title = news.getTitle();
            this.publishedAt = news.getPublishedAt();
            this.provider = news.getProvider();
        }
    }

    @Getter
    public static class TopCategoryAnalysisResponse {
        private final Long categoryId;
        private final String categoryName;
        private final long newsCount;
        private final String analysis;

        public TopCategoryAnalysisResponse(Long categoryId, String categoryName, long newsCount, String analysis) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.newsCount = newsCount;
            this.analysis = analysis;
        }
    }
}
