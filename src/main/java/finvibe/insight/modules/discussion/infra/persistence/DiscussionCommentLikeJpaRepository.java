package finvibe.insight.modules.discussion.infra.persistence;

import finvibe.insight.modules.discussion.domain.DiscussionCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DiscussionCommentLikeJpaRepository extends JpaRepository<DiscussionCommentLike, Long> {
    long countByCommentId(Long commentId);

    Optional<DiscussionCommentLike> findByCommentIdAndUserId(Long commentId, UUID userId);
}
