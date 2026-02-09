package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.application.port.out.MarketCategoryChangeRatePort;
import finvibe.insight.modules.news.application.port.out.ThemeAnalyzer;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.application.port.in.ThemeQueryUseCase;
import finvibe.insight.modules.news.application.port.out.ThemeDailyRepository;
import finvibe.insight.modules.news.domain.ThemeDaily;
import finvibe.insight.modules.news.domain.error.ThemeErrorCode;
import finvibe.insight.modules.news.dto.ThemeDto;
import finvibe.insight.shared.domain.Category;
import finvibe.insight.shared.error.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeQueryService implements ThemeQueryUseCase {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final int ANALYSIS_NEWS_LIMIT = 20;

    private final ThemeDailyRepository themeDailyRepository;
    private final NewsRepository newsRepository;
    private final MarketCategoryChangeRatePort marketCategoryChangeRatePort;
    private final ThemeAnalyzer themeAnalyzer;

    @Override
    public List<ThemeDto.SummaryResponse> findTodayThemes() {
        LocalDate today = LocalDate.now(KST_ZONE);
        return themeDailyRepository.findAllByThemeDate(today).stream()
                .map(themeDaily -> new ThemeDto.SummaryResponse(
                        themeDaily,
                        marketCategoryChangeRatePort.fetchAverageChangePct(
                                themeDaily.getCategory().getId())))
                .toList();
    }

    @Override
    public ThemeDto.DetailResponse findTodayThemeDetail(Long categoryId) {
        LocalDate today = LocalDate.now(KST_ZONE);
        ThemeDaily themeDaily = themeDailyRepository.findByThemeDateAndCategoryId(today, categoryId)
                .orElseThrow(() -> new DomainException(ThemeErrorCode.THEME_NOT_FOUND));

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);
        List<ThemeDto.NewsSummary> news = newsRepository
                .findAllByCategoryIdAndPublishedAtBetweenOrderByPublishedAtDesc(categoryId, start, end)
                .stream()
                .map(ThemeDto.NewsSummary::new)
                .toList();

        return new ThemeDto.DetailResponse(themeDaily, news);
    }

    @Override
    public ThemeDto.TopCategoryAnalysisResponse findTopCategoryAnalysis() {
        Map<Category, Long> categoryCounts = newsRepository.findAll().stream()
                .map(News::getCategory)
                .filter(category -> category != null && category.getId() != null)
                .collect(Collectors.groupingBy(category -> category, Collectors.counting()));

        Map.Entry<Category, Long> topCategory = categoryCounts.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .orElseThrow(() -> new DomainException(ThemeErrorCode.TOP_CATEGORY_NOT_FOUND));

        Category category = topCategory.getKey();
        long newsCount = topCategory.getValue();

        List<String> newsTitles = newsRepository.findAllByCategoryIdOrderByPublishedAtDesc(category.getId()).stream()
                .map(News::getTitle)
                .limit(ANALYSIS_NEWS_LIMIT)
                .toList();

        String analysis = themeAnalyzer.analyze(category, newsTitles);
        return new ThemeDto.TopCategoryAnalysisResponse(
                category.getId(),
                category.getName(),
                newsCount,
                analysis);
    }
}
