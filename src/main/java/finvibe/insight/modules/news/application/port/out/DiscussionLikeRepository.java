package finvibe.insight.modules.news.application.port.out;

import finvibe.insight.modules.news.domain.DiscussionLike;

import java.util.Optional;
import java.util.UUID;

public interface DiscussionLikeRepository {
    long countByDiscussionId(Long discussionId);

    DiscussionLike save(DiscussionLike like);

    void delete(DiscussionLike like);

    Optional<DiscussionLike> findByDiscussionIdAndUserId(Long discussionId, UUID userId);
}
