package finvibe.insight.modules.news.application.port.in;

import finvibe.insight.modules.news.dto.ThemeDto;

import java.util.List;

public interface ThemeQueryUseCase {
    List<ThemeDto.SummaryResponse> findTodayThemes();

    ThemeDto.DetailResponse findTodayThemeDetail(Long categoryId);

    ThemeDto.TopCategoryAnalysisResponse findTopCategoryAnalysis();
}
