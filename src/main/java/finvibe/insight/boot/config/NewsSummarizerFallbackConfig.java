package finvibe.insight.boot.config;

import finvibe.insight.modules.news.application.port.out.NewsSummarizer;
import finvibe.insight.modules.news.domain.EconomicSignal;
import finvibe.insight.modules.news.domain.NewsKeyword;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NewsSummarizerFallbackConfig {

    @Bean
    @ConditionalOnMissingBean(NewsSummarizer.class)
    public NewsSummarizer fallbackNewsSummarizer() {
        return content -> new NewsSummarizer.AnalysisResult(
                "AI summary not available in this environment.",
                EconomicSignal.NEUTRAL,
                NewsKeyword.ETF
        );
    }
}
