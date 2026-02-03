package finvibe.insight.modules.news.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class NewsKeywordTest {

    @Test
    @DisplayName("NewsKeyword enum should have correct labels")
    void newsKeywordLabels() {
        assertThat(NewsKeyword.SEMICONDUCTOR.getLabel()).isEqualTo("반도체");
        assertThat(NewsKeyword.ETF.getLabel()).isEqualTo("ETF");
        assertThat(NewsKeyword.EARNINGS_SHOCK.getLabel()).isEqualTo("어닝쇼크");
    }

    @ParameterizedTest
    @CsvSource({
            "SEMICONDUCTOR, SEMICONDUCTOR",
            "semiconductor, SEMICONDUCTOR",
            "반도체, SEMICONDUCTOR",
            "ETF, ETF",
            "etf, ETF",
            "UNKNOWN, THEME_STOCK"
    })
    @DisplayName("fromString should resolve name/label or fallback")
    void fromStringResolves(String input, NewsKeyword expected) {
        assertThat(NewsKeyword.fromString(input)).isEqualTo(expected);
    }
}
