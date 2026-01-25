package finvibe.insight.modules.news.application.port.out;

import finvibe.insight.modules.news.domain.EconomicSignal;

public interface NewsSummarizer {
    AnalysisResult analyzeAndSummarize(String content);

    record AnalysisResult(String summary, EconomicSignal signal) {
    }
}
