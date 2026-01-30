package finvibe.insight.modules.news.infra.summarizer;

import finvibe.insight.modules.news.application.port.out.NewsSummarizer;
import finvibe.insight.modules.news.domain.EconomicSignal;
import finvibe.insight.modules.news.domain.NewsKeyword;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class RuleBasedNewsSummarizer implements NewsSummarizer {

    private static final int SUMMARY_LIMIT = 180;

    private static final List<String> POSITIVE_TOKENS = List.of(
            "상승", "급등", "호재", "개선", "증가", "사상 최고", "기대", "낙관", "강세");

    private static final List<String> NEGATIVE_TOKENS = List.of(
            "하락", "급락", "악재", "부진", "감소", "경기침체", "위축", "불확실", "약세");

    private static final Map<NewsKeyword, List<String>> KEYWORD_RULES = new LinkedHashMap<>();

    static {
        KEYWORD_RULES.put(NewsKeyword.반도체, List.of("반도체", "메모리", "DRAM", "낸드"));
        KEYWORD_RULES.put(NewsKeyword.이차전지, List.of("이차전지", "배터리"));
        KEYWORD_RULES.put(NewsKeyword.AI, List.of("AI", "인공지능"));
        KEYWORD_RULES.put(NewsKeyword.전기차, List.of("전기차", "EV"));
        KEYWORD_RULES.put(NewsKeyword.바이오, List.of("바이오", "제약", "신약"));
        KEYWORD_RULES.put(NewsKeyword.플랫폼, List.of("플랫폼", "핀테크"));
        KEYWORD_RULES.put(NewsKeyword.우주항공, List.of("우주", "항공", "로켓"));
        KEYWORD_RULES.put(NewsKeyword.엔터테인먼트, List.of("엔터", "콘텐츠", "게임"));
        KEYWORD_RULES.put(NewsKeyword.금리동결, List.of("금리 동결", "동결"));
        KEYWORD_RULES.put(NewsKeyword.금리인상, List.of("금리 인상", "인상"));
        KEYWORD_RULES.put(NewsKeyword.금리인하, List.of("금리 인하", "인하"));
        KEYWORD_RULES.put(NewsKeyword.인플레이션, List.of("인플레이션", "물가"));
        KEYWORD_RULES.put(NewsKeyword.환율, List.of("환율", "원/달러"));
        KEYWORD_RULES.put(NewsKeyword.FOMC, List.of("FOMC", "연준"));
        KEYWORD_RULES.put(NewsKeyword.경기침체, List.of("경기침체", "리세션"));
        KEYWORD_RULES.put(NewsKeyword.부동산, List.of("부동산", "주택"));
        KEYWORD_RULES.put(NewsKeyword.유가, List.of("유가", "WTI", "브렌트"));
        KEYWORD_RULES.put(NewsKeyword.배당주, List.of("배당", "배당주"));
        KEYWORD_RULES.put(NewsKeyword.성장주, List.of("성장주", "그로스"));
        KEYWORD_RULES.put(NewsKeyword.가치주, List.of("가치주", "밸류"));
        KEYWORD_RULES.put(NewsKeyword.ETF, List.of("ETF", "상장지수"));
        KEYWORD_RULES.put(NewsKeyword.공모주, List.of("공모주", "IPO"));
        KEYWORD_RULES.put(NewsKeyword.테마주, List.of("테마주", "테마"));
        KEYWORD_RULES.put(NewsKeyword.실적발표, List.of("실적 발표", "실적발표"));
        KEYWORD_RULES.put(NewsKeyword.어닝서프라이즈, List.of("어닝 서프라이즈", "서프라이즈"));
        KEYWORD_RULES.put(NewsKeyword.어닝쇼크, List.of("어닝 쇼크", "쇼크"));
        KEYWORD_RULES.put(NewsKeyword.M_AND_A, List.of("M&A", "인수", "합병"));
        KEYWORD_RULES.put(NewsKeyword.무상증자, List.of("무상증자"));
        KEYWORD_RULES.put(NewsKeyword.유상증자, List.of("유상증자"));
        KEYWORD_RULES.put(NewsKeyword.주주총회, List.of("주주총회"));
        KEYWORD_RULES.put(NewsKeyword.배당락일, List.of("배당락", "배당락일"));
    }

    @Override
    public AnalysisResult analyzeAndSummarize(String content) {
        String normalized = content == null ? "" : content;
        EconomicSignal signal = resolveSignal(normalized);
        NewsKeyword keyword = resolveKeyword(normalized);
        String summary = summarize(normalized);
        return new AnalysisResult(summary, signal, keyword);
    }

    private static EconomicSignal resolveSignal(String content) {
        String haystack = content.toLowerCase(Locale.ROOT);
        if (containsAny(haystack, POSITIVE_TOKENS)) {
            return EconomicSignal.POSITIVE;
        }
        if (containsAny(haystack, NEGATIVE_TOKENS)) {
            return EconomicSignal.NEGATIVE;
        }
        return EconomicSignal.NEUTRAL;
    }

    private static NewsKeyword resolveKeyword(String content) {
        String haystack = content.toLowerCase(Locale.ROOT);
        for (Map.Entry<NewsKeyword, List<String>> entry : KEYWORD_RULES.entrySet()) {
            if (containsAny(haystack, entry.getValue())) {
                return entry.getKey();
            }
        }
        return NewsKeyword.테마주;
    }

    private static String summarize(String content) {
        String trimmed = content.replaceAll("\\s+", " ").trim();
        if (trimmed.length() <= SUMMARY_LIMIT) {
            return trimmed;
        }
        return trimmed.substring(0, SUMMARY_LIMIT) + "...";
    }

    private static boolean containsAny(String haystack, List<String> tokens) {
        for (String token : tokens) {
            if (haystack.contains(token.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
