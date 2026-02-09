package finvibe.insight.modules.news.domain.error;

import finvibe.insight.shared.error.DomainErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ThemeErrorCode implements DomainErrorCode {
    THEME_NOT_FOUND("THEME_NOT_FOUND", "오늘의 테마를 찾을 수 없습니다."),
    TOP_CATEGORY_NOT_FOUND("TOP_CATEGORY_NOT_FOUND", "분석할 상위 카테고리 뉴스가 없습니다.");

    private final String code;
    private final String message;
}
