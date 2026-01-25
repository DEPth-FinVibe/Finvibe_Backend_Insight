package finvibe.insight.modules.news.domain;

import finvibe.insight.shared.domain.TimeStampedBaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class News extends TimeStampedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private String analysis;

    private String category;

    @Enumerated(EnumType.STRING)
    private EconomicSignal economicSignal;

    public static News create(String title, String content, String category, String analysis,
            EconomicSignal economicSignal) {
        return News.builder()
                .title(title)
                .content(content)
                .category(category)
                .analysis(analysis)
                .economicSignal(economicSignal)
                .build();
    }

    public void updateAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public void changeSignal(EconomicSignal economicSignal) {
        this.economicSignal = economicSignal;
    }
}
