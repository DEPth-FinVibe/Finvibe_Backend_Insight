package finvibe.insight.modules.news.domain;

import finvibe.insight.shared.domain.Category;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "theme_daily")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ThemeDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate themeDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String analysis;

    public static ThemeDaily create(LocalDate themeDate, Category category, String analysis) {
        return ThemeDaily.builder()
                .themeDate(themeDate)
                .category(category)
                .analysis(analysis)
                .build();
    }
}
