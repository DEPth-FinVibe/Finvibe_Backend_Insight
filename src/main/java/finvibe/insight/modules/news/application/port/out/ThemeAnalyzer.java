package finvibe.insight.modules.news.application.port.out;

import finvibe.insight.shared.domain.Category;

import java.util.List;

public interface ThemeAnalyzer {
    String analyze(Category category, List<String> newsTitles);
}
