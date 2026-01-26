package finvibe.insight.modules.news.application.port.out;

import finvibe.insight.modules.news.domain.DiscussionComment;

import java.util.List;
import java.util.Optional;

public interface DiscussionCommentRepository {
    List<DiscussionComment> findAllByDiscussionIdOrderByCreatedAtAsc(Long discussionId);

    DiscussionComment save(DiscussionComment comment);

    Optional<DiscussionComment> findById(Long id);
}
