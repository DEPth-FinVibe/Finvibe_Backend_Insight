package finvibe.insight.modules.discussion.infra.persistence;

import finvibe.insight.modules.discussion.domain.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscussionJpaRepository extends JpaRepository<Discussion, Long> {
    long countByNewsId(Long newsId);

    List<Discussion> findAllByNewsIdOrderByCreatedAtAsc(Long newsId);

    List<Discussion> findAllByOrderByCreatedAtDesc();
}
