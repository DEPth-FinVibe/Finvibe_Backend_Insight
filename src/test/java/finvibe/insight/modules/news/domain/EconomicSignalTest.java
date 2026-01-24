package finvibe.insight.modules.news.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class EconomicSignalTest {

    @Test
    @DisplayName("EconomicSignal enum should have correct metadata")
    void economicSignalMetadataTest() {
        assertThat(EconomicSignal.POSITIVE.getLabel()).isEqualTo("호재");
        assertThat(EconomicSignal.NEGATIVE.getLabel()).isEqualTo("악재");
        assertThat(EconomicSignal.NEUTRAL.getLabel()).isEqualTo("중립");
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, POSITIVE",
            "positive, POSITIVE",
            "호재, POSITIVE",
            "NEGATIVE, NEGATIVE",
            "negative, NEGATIVE",
            "악재, NEGATIVE",
            "NEUTRAL, NEUTRAL",
            "neutral, NEUTRAL",
            "중립, NEUTRAL",
            "UNKNOWN, NEUTRAL"
    })
    @DisplayName("fromString should correctly parse various inputs")
    void fromStringTest(String input, EconomicSignal expected) {
        assertThat(EconomicSignal.fromString(input)).isEqualTo(expected);
    }
}
