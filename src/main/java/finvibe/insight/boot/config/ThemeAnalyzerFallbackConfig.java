package finvibe.insight.boot.config;

import finvibe.insight.modules.news.application.port.out.ThemeAnalyzer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThemeAnalyzerFallbackConfig {

    @Bean
    @ConditionalOnMissingBean(ThemeAnalyzer.class)
    public ThemeAnalyzer fallbackThemeAnalyzer() {
        return (category, newsTitles) -> category.name() + " 이슈 요약";
    }
}
