package finvibe.insight.modules.discussion.infra.persistence;

import finvibe.insight.modules.discussion.application.port.out.DiscussionCommentLikeRepository;
import finvibe.insight.modules.discussion.domain.DiscussionCommentLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DiscussionCommentLikeRepositoryImpl implements DiscussionCommentLikeRepository {

    private final DiscussionCommentLikeJpaRepository discussionCommentLikeJpaRepository;

    @Override
    public long countByCommentId(Long commentId) {
        return discussionCommentLikeJpaRepository.countByCommentId(commentId);
    }

    @Override
    public DiscussionCommentLike save(DiscussionCommentLike like) {
        return discussionCommentLikeJpaRepository.save(like);
    }

    @Override
    public void delete(DiscussionCommentLike like) {
        discussionCommentLikeJpaRepository.delete(like);
    }

    @Override
    public Optional<DiscussionCommentLike> findByCommentIdAndUserId(Long commentId, UUID userId) {
        return discussionCommentLikeJpaRepository.findByCommentIdAndUserId(commentId, userId);
    }
}
