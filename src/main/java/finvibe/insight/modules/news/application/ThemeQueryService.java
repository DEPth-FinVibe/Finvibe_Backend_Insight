package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.application.port.out.MarketCategoryChangeRatePort;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.application.port.in.ThemeQueryUseCase;
import finvibe.insight.modules.news.application.port.out.ThemeDailyRepository;
import finvibe.insight.modules.news.domain.ThemeDaily;
import finvibe.insight.modules.news.domain.error.ThemeErrorCode;
import finvibe.insight.modules.news.dto.ThemeDto;
import finvibe.insight.shared.error.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeQueryService implements ThemeQueryUseCase {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    private final ThemeDailyRepository themeDailyRepository;
    private final NewsRepository newsRepository;
    private final MarketCategoryChangeRatePort marketCategoryChangeRatePort;

    @Override
    public List<ThemeDto.SummaryResponse> findTodayThemes() {
        LocalDate today = LocalDate.now(KST_ZONE);
        return themeDailyRepository.findAllByThemeDate(today).stream()
                .map(themeDaily -> new ThemeDto.SummaryResponse(
                        themeDaily,
                        marketCategoryChangeRatePort.fetchAverageChangePct(
                                themeDaily.getCategoryId())))
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
}
