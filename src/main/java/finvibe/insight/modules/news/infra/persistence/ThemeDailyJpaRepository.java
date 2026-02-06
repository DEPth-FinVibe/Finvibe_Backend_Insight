package finvibe.insight.modules.news.infra.persistence;

import finvibe.insight.modules.news.domain.ThemeDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeDailyJpaRepository extends JpaRepository<ThemeDaily, Long> {
    List<ThemeDaily> findAllByThemeDate(LocalDate themeDate);

    Optional<ThemeDaily> findByThemeDateAndCategoryId(LocalDate themeDate, Long categoryId);

    void deleteAllByThemeDate(LocalDate themeDate);
}
