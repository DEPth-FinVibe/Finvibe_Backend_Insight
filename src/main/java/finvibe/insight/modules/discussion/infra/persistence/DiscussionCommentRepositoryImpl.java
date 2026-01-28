package finvibe.insight.modules.discussion.infra.persistence;

import finvibe.insight.modules.discussion.application.port.out.DiscussionCommentRepository;
import finvibe.insight.modules.discussion.domain.DiscussionComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DiscussionCommentRepositoryImpl implements DiscussionCommentRepository {

    private final DiscussionCommentJpaRepository jpaRepository;

    @Override
    public List<DiscussionComment> findAllByDiscussionIdOrderByCreatedAtAsc(Long discussionId) {
        return jpaRepository.findAllByDiscussionIdOrderByCreatedAtAsc(discussionId);
    }

    @Override
    public DiscussionComment save(DiscussionComment comment) {
        return jpaRepository.save(comment);
    }

    @Override
    public Optional<DiscussionComment> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void delete(DiscussionComment comment) {
        jpaRepository.delete(comment);
    }
}
