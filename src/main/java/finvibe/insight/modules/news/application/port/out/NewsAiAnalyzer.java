package finvibe.insight.modules.news.application.port.out;

import finvibe.insight.modules.news.domain.EconomicSignal;
import finvibe.insight.modules.news.domain.NewsKeyword;
import finvibe.insight.shared.domain.CategoryInfo;

import java.util.List;

public interface NewsAiAnalyzer {
    AnalysisResult analyze(String content, List<CategoryInfo> categories);

    record AnalysisResult(String summary, EconomicSignal signal, NewsKeyword keyword, Long categoryId) {
    }
}
