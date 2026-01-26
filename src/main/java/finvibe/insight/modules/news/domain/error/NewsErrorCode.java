package finvibe.insight.modules.news.domain.error;

import finvibe.insight.shared.error.DomainErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NewsErrorCode implements DomainErrorCode {

    NEWS_NOT_FOUND("NEWS_NOT_FOUND", "존재하지 않는 뉴스입니다.");

    private final String code;
    private final String message;
}
