package finvibe.insight.modules.news.application.port.out;

import finvibe.insight.modules.news.domain.ThemeDaily;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeDailyRepository {
    ThemeDaily save(ThemeDaily themeDaily);

    List<ThemeDaily> findAllByThemeDate(LocalDate themeDate);

    Optional<ThemeDaily> findByThemeDateAndCategoryId(LocalDate themeDate, Long categoryId);

    void deleteAllByThemeDate(LocalDate themeDate);
}
