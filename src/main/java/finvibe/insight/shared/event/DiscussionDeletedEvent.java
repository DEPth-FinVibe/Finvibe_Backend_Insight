package finvibe.insight.shared.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionDeletedEvent {
    private Long discussionId;
    private Long newsId; // Nullable
}
