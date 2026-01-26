package finvibe.insight.modules.discussion.application.port.out;

import finvibe.insight.modules.discussion.domain.DiscussionComment;

import java.util.List;
import java.util.Optional;

public interface DiscussionCommentRepository {
    List<DiscussionComment> findAllByDiscussionIdOrderByCreatedAtAsc(Long discussionId);

    DiscussionComment save(DiscussionComment comment);

    Optional<DiscussionComment> findById(Long id);
}
