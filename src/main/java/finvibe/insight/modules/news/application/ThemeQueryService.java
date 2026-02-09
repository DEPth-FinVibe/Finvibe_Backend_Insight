package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.application.port.out.MarketCategoryChangeRatePort;
import finvibe.insight.modules.news.application.port.in.ThemeQueryUseCase;
import finvibe.insight.modules.news.application.port.out.ThemeDailyRepository;
import finvibe.insight.modules.news.domain.ThemeDaily;
import finvibe.insight.modules.news.domain.error.ThemeErrorCode;
import finvibe.insight.modules.news.dto.ThemeDto;
import finvibe.insight.shared.error.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeQueryService implements ThemeQueryUseCase {

    private final ThemeDailyRepository themeDailyRepository;
    private final NewsRepository newsRepository;
    private final MarketCategoryChangeRatePort marketCategoryChangeRatePort;

    @Override
    public List<ThemeDto.SummaryResponse> findTodayThemes() {
        return themeDailyRepository.findDistinctCategoryIds().stream()
                .map(themeDailyRepository::findLatestByCategoryId)
                .flatMap(Optional::stream)
                .map(themeDaily -> new ThemeDto.SummaryResponse(
                        themeDaily,
                        marketCategoryChangeRatePort.fetchAverageChangePct(
                                themeDaily.getCategoryId())))
                .toList();
    }

    @Override
    public ThemeDto.DetailResponse findTodayThemeDetail(Long categoryId) {
        ThemeDaily themeDaily = themeDailyRepository.findLatestByCategoryId(categoryId)
                .orElseThrow(() -> new DomainException(ThemeErrorCode.THEME_NOT_FOUND));

        LocalDateTime themeDateStart = themeDaily.getThemeDate().atStartOfDay();
        LocalDateTime themeDateEnd = themeDateStart.plusDays(1).minusNanos(1);
        List<ThemeDto.NewsSummary> news = newsRepository
                .findAllByCategoryIdAndPublishedAtBetweenOrderByPublishedAtDesc(categoryId, themeDateStart, themeDateEnd)
                .stream()
                .map(ThemeDto.NewsSummary::new)
                .toList();

        return new ThemeDto.DetailResponse(themeDaily, news);
    }
}
