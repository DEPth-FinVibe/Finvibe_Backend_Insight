package finvibe.insight.modules.news.infra.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import finvibe.insight.modules.news.application.port.out.NewsSummarizer;
import finvibe.insight.modules.news.domain.EconomicSignal;
import finvibe.insight.modules.news.domain.NewsKeyword;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NewsAnalysisParser {

    private final ObjectMapper objectMapper;

    public NewsAnalysisParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public NewsSummarizer.AnalysisResult parse(String response) {
        try {
            RawAnalysisResponse raw = objectMapper.readValue(response, RawAnalysisResponse.class);
            return new NewsSummarizer.AnalysisResult(
                    raw.summary(),
                    EconomicSignal.fromString(raw.signal()),
                    NewsKeyword.fromString(raw.keyword()));
        } catch (Exception ex) {
            log.error("Failed to parse AI response: {}", response, ex);
            throw new RuntimeException("AI response parsing failed", ex);
        }
    }

    private record RawAnalysisResponse(String summary, String signal, String keyword) {
    }
}
