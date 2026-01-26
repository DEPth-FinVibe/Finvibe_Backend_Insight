package finvibe.insight.modules.discussion.domain.error;

import finvibe.insight.shared.error.DomainErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DiscussionErrorCode implements DomainErrorCode {

    DISCUSSION_NOT_FOUND("DISCUSSION_NOT_FOUND", "존재하지 않는 토론입니다."),
    COMMENT_NOT_FOUND("COMMENT_NOT_FOUND", "존재하지 않는 댓글입니다.");

    private final String code;
    private final String message;
}
