package finvibe.insight.modules.discussion.application.port.out;

import finvibe.insight.shared.event.DiscussionCreatedEvent;
import finvibe.insight.shared.event.DiscussionDeletedEvent;

public interface DiscussionEventPublisher {
    void publish(DiscussionCreatedEvent event);

    void publish(DiscussionDeletedEvent event);
}
