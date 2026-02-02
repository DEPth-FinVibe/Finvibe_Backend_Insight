package finvibe.insight.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionEvent {
    private DiscussionEventType type;
    private Long newsId;
}
