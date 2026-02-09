package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.out.MarketCategoryChangeRatePort;
import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.application.port.out.ThemeAnalyzer;
import finvibe.insight.modules.news.application.port.out.ThemeDailyRepository;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.domain.error.ThemeErrorCode;
import finvibe.insight.modules.news.dto.ThemeDto;
import finvibe.insight.shared.domain.Category;
import finvibe.insight.shared.error.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThemeQueryServiceTest {

    @Mock
    private ThemeDailyRepository themeDailyRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private MarketCategoryChangeRatePort marketCategoryChangeRatePort;

    @Mock
    private ThemeAnalyzer themeAnalyzer;

    @InjectMocks
    private ThemeQueryService themeQueryService;

    @Test
    @DisplayName("findTopCategoryAnalysis returns AI analysis for most frequent category in all news")
    void findTopCategoryAnalysisReturnsTopCategoryInsight() {
        Category topCategory = mock(Category.class);
        when(topCategory.getId()).thenReturn(10L);
        when(topCategory.getName()).thenReturn("반도체");

        Category otherCategory = mock(Category.class);
        when(otherCategory.getId()).thenReturn(20L);

        News a1 = News.builder().id(1L).category(topCategory).createdAt(LocalDateTime.now()).build();
        News a2 = News.builder().id(2L).category(topCategory).createdAt(LocalDateTime.now()).build();
        News b1 = News.builder().id(3L).category(otherCategory).createdAt(LocalDateTime.now()).build();

        when(newsRepository.findAll()).thenReturn(List.of(a1, a2, b1));
        when(newsRepository.findAllByCategoryIdOrderByPublishedAtDesc(10L))
                .thenReturn(List.of(
                        News.builder().id(11L).title("title-1").publishedAt(LocalDateTime.now()).build(),
                        News.builder().id(12L).title("title-2").publishedAt(LocalDateTime.now().minusMinutes(1)).build()));
        when(themeAnalyzer.analyze(topCategory, List.of("title-1", "title-2")))
                .thenReturn("반도체 이슈 요약");

        ThemeDto.TopCategoryAnalysisResponse result = themeQueryService.findTopCategoryAnalysis();

        assertThat(result.getCategoryId()).isEqualTo(10L);
        assertThat(result.getCategoryName()).isEqualTo("반도체");
        assertThat(result.getNewsCount()).isEqualTo(2);
        assertThat(result.getAnalysis()).isEqualTo("반도체 이슈 요약");
    }

    @Test
    @DisplayName("findTopCategoryAnalysis throws when no categorized news exists")
    void findTopCategoryAnalysisThrowsWhenNoCategorizedNews() {
        News uncategorized = News.builder().id(1L).category(null).createdAt(LocalDateTime.now()).build();
        when(newsRepository.findAll()).thenReturn(List.of(uncategorized));

        assertThatThrownBy(() -> themeQueryService.findTopCategoryAnalysis())
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException domainException = (DomainException) ex;
                    assertThat(domainException.getErrorCode()).isEqualTo(ThemeErrorCode.TOP_CATEGORY_NOT_FOUND);
                });
    }
}
