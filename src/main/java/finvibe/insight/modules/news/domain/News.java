package finvibe.insight.modules.news.domain;

import finvibe.insight.shared.domain.Category;
import finvibe.insight.shared.domain.TimeStampedBaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class News extends TimeStampedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private long discussionCount = 0;

    private String title;

    private String content;

    private String analysis;

    @Enumerated(EnumType.STRING)
    private EconomicSignal economicSignal;

    @Enumerated(EnumType.STRING)
    private NewsKeyword keyword;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private LocalDateTime publishedAt;

    private String provider;

    public static News create(String title, String content, String analysis,
            EconomicSignal economicSignal, NewsKeyword keyword, Category category,
            LocalDateTime publishedAt, String provider) {
        return News.builder()
                .title(title)
                .content(content)
                .analysis(analysis)
                .economicSignal(economicSignal)
                .keyword(keyword)
                .category(category)
                .publishedAt(publishedAt)
                .provider(provider)
                .build();
    }

    public void updateAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public void changeSignal(EconomicSignal economicSignal) {
        this.economicSignal = economicSignal;
    }

    public void incrementDiscussionCount() {
        this.discussionCount++;
    }

    public void decrementDiscussionCount() {
        if (this.discussionCount > 0) {
            this.discussionCount--;
        }
    }

    public void syncDiscussionCount(long count) {
        this.discussionCount = count;
    }
}
