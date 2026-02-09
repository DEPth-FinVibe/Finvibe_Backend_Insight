package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.application.port.in.ThemeCommandUseCase;
import finvibe.insight.modules.news.application.port.out.ThemeAnalyzer;
import finvibe.insight.modules.news.application.port.out.ThemeDailyRepository;
import finvibe.insight.modules.news.domain.ThemeDaily;
import finvibe.insight.modules.news.application.port.out.CategoryCatalogPort;
import finvibe.insight.shared.domain.CategoryInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ThemeCommandService implements ThemeCommandUseCase {

    private static final int THEME_LIMIT = 8;
    private static final int ANALYSIS_NEWS_LIMIT = 20;
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    private final NewsRepository newsRepository;
    private final CategoryCatalogPort categoryCatalogPort;
    private final ThemeAnalyzer themeAnalyzer;
    private final ThemeDailyRepository themeDailyRepository;

    @Override
    public void generateTodayThemes() {
        LocalDate today = LocalDate.now(KST_ZONE);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        List<NewsRepository.NewsCategoryCount> counts =
                newsRepository.countByCategoryIdForPeriod(start, end);
        List<Long> topCategoryIds = counts.stream()
                .limit(THEME_LIMIT)
                .map(NewsRepository.NewsCategoryCount::categoryId)
                .toList();

        Map<Long, CategoryInfo> categoryMap = categoryCatalogPort.getAll().stream()
                .collect(Collectors.toMap(CategoryInfo::id, category -> category));

        themeDailyRepository.deleteAllByThemeDate(today);

        for (Long categoryId : topCategoryIds) {
            CategoryInfo category = categoryMap.get(categoryId);
            if (category == null) {
                continue;
            }

            List<String> titles = newsRepository
                    .findAllByCategoryIdAndPublishedAtBetweenOrderByPublishedAtDesc(categoryId, start, end)
                    .stream()
                    .map(News::getTitle)
                    .limit(ANALYSIS_NEWS_LIMIT)
                    .toList();

            String analysis = themeAnalyzer.analyze(category, titles);
            ThemeDaily themeDaily = ThemeDaily.create(today, category.id(), category.name(), analysis);
            themeDailyRepository.save(themeDaily);
        }
    }
}
