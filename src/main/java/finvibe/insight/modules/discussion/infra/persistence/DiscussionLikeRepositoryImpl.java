package finvibe.insight.modules.discussion.infra.persistence;

import finvibe.insight.modules.discussion.application.port.out.DiscussionLikeRepository;
import finvibe.insight.modules.discussion.domain.DiscussionLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DiscussionLikeRepositoryImpl implements DiscussionLikeRepository {

    private final DiscussionLikeJpaRepository discussionLikeJpaRepository;

    @Override
    public long countByDiscussionId(Long discussionId) {
        return discussionLikeJpaRepository.countByDiscussionId(discussionId);
    }

    @Override
    public DiscussionLike save(DiscussionLike like) {
        return discussionLikeJpaRepository.save(like);
    }

    @Override
    public void delete(DiscussionLike like) {
        discussionLikeJpaRepository.delete(like);
    }

    @Override
    public Optional<DiscussionLike> findByDiscussionIdAndUserId(Long discussionId, UUID userId) {
        return discussionLikeJpaRepository.findByDiscussionIdAndUserId(discussionId, userId);
    }
}
