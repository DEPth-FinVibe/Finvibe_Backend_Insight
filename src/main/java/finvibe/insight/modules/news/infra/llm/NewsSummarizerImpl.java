package finvibe.insight.modules.news.infra.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import finvibe.insight.modules.news.application.port.out.NewsSummarizer;
import finvibe.insight.modules.news.domain.EconomicSignal;
import finvibe.insight.modules.news.domain.NewsKeyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsSummarizerImpl implements NewsSummarizer {

    private static final String SYSTEM_PROMPT_PATH = "classpath:prompts/news-analysis-system.txt";
    private static final String USER_PROMPT_PATH = "classpath:prompts/news-analysis-user.txt";
    private static final int MAX_RETRY_COUNT = 2;

    private static final ResponseFormat RESPONSE_FORMAT = ResponseFormat.builder()
            .type(ResponseFormatType.JSON)
            .jsonSchema(JsonSchema.builder()
                    .rootElement(JsonObjectSchema.builder()
                            .addStringProperty("summary")
                            .addStringProperty("signal")
                            .addStringProperty("keyword")
                            .required("summary", "signal", "keyword")
                            .build())
                    .build())
            .build();

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Override
    public AnalysisResult analyzeAndSummarize(String content) {
        for (int attempt = 0; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                return requestAnalysis(content);
            } catch (Exception e) {
                log.warn("Failed to analyze news (attempt {}): {}", attempt + 1, e.getMessage());
                if (attempt == MAX_RETRY_COUNT) {
                    break;
                }
            }
        }
        return fallbackResult(content);
    }

    private AnalysisResult requestAnalysis(String content) {
        String keywordList = Arrays.stream(NewsKeyword.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        String systemMessage = loadPrompt(SYSTEM_PROMPT_PATH)
                .replace("{{keyword_list}}", keywordList);

        String userMessage = loadPrompt(USER_PROMPT_PATH)
                .replace("{{news_content}}", content);

        ChatRequest request = ChatRequest.builder()
                .messages(
                        SystemMessage.from(systemMessage),
                        UserMessage.from(userMessage))
                .responseFormat(RESPONSE_FORMAT)
                .build();

        ChatResponse chatResponse = chatModel.chat(request);
        String responseText = chatResponse.aiMessage().text();

        return parseResponse(responseText);
    }

    private AnalysisResult parseResponse(String response) {
        try {
            // LangChain4j가 반환한 JSON을 중간 DTO로 파싱 후 도메인 레코드로 변환
            RawAnalysisResponse raw = objectMapper.readValue(response, RawAnalysisResponse.class);
            return new AnalysisResult(
                    raw.summary(),
                    EconomicSignal.fromString(raw.signal()),
                    NewsKeyword.valueOf(raw.keyword()));
        } catch (Exception ex) {
            log.error("Failed to parse AI response: {}", response, ex);
            throw new RuntimeException("AI response parsing failed", ex);
        }
    }

    private String loadPrompt(String path) {
        Resource resource = resourceLoader.getResource(path);
        try {
            byte[] bytes = resource.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8).trim();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load prompt: " + path, ex);
        }
    }

    private AnalysisResult fallbackResult(String content) {
        return new AnalysisResult(
                "뉴스 분석에 실패하여 요약 정보를 제공할 수 없습니다.",
                EconomicSignal.NEUTRAL,
                NewsKeyword.ETF // 기본값
        );
    }

    // JSON 파싱을 위한 뉴스 분석 응답 스키마
    private record RawAnalysisResponse(String summary, String signal, String keyword) {
    }
}
