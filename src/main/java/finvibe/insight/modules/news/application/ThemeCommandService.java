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

@Service
@RequiredArgsConstructor
@Transactional
public class ThemeCommandService implements ThemeCommandUseCase {

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

        List<CategoryInfo> categories = categoryCatalogPort.getAll();

        themeDailyRepository.deleteAllByThemeDate(today);

        for (CategoryInfo category : categories) {
            List<String> titles = newsRepository
                    .findAllByCategoryIdAndPublishedAtBetweenOrderByPublishedAtDesc(category.id(), start, end)
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
