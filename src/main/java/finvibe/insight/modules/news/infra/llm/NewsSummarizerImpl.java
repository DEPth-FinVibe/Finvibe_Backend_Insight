package finvibe.insight.modules.news.infra.llm;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "gemini.api-key")
public class NewsSummarizerImpl implements NewsSummarizer {

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
    private final NewsPromptProvider promptProvider;
    private final NewsAnalysisParser analysisParser;

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
                .map(k -> String.format("%s(%s)", k.name(), k.getLabel()))
                .collect(Collectors.joining(", "));

        String systemMessage = promptProvider.getSystemPrompt(keywordList);
        String userMessage = promptProvider.getUserPrompt(content);

        ChatRequest request = ChatRequest.builder()
                .messages(
                        SystemMessage.from(systemMessage),
                        UserMessage.from(userMessage))
                .responseFormat(RESPONSE_FORMAT)
                .build();

        ChatResponse chatResponse = chatModel.chat(request);
        String responseText = chatResponse.aiMessage().text();

        return analysisParser.parse(responseText);
    }

    private AnalysisResult fallbackResult(String content) {
        return new AnalysisResult(
                "뉴스 분석에 실패하여 요약 정보를 제공할 수 없습니다.",
                EconomicSignal.NEUTRAL,
                NewsKeyword.ETF // 기본값
        );
    }

}
