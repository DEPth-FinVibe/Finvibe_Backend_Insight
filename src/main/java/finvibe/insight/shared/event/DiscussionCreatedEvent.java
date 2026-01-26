package finvibe.insight.shared.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionCreatedEvent {
    private Long discussionId;
    private Long newsId; // Nullable (뉴스 없는 토론의 경우)
}
