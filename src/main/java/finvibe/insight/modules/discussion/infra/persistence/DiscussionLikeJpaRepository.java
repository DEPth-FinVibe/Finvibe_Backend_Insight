package finvibe.insight.modules.discussion.infra.persistence;

import finvibe.insight.modules.discussion.domain.DiscussionLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DiscussionLikeJpaRepository extends JpaRepository<DiscussionLike, Long> {
    long countByDiscussionId(Long discussionId);

    Optional<DiscussionLike> findByDiscussionIdAndUserId(Long discussionId, UUID userId);
}
