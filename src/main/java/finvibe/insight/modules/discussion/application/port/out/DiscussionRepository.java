package finvibe.insight.modules.discussion.application.port.out;

import finvibe.insight.modules.discussion.domain.Discussion;

import java.util.List;
import java.util.Optional;

public interface DiscussionRepository {
    long countByNewsId(Long newsId);

    List<Discussion> findAllByNewsIdOrderByCreatedAtAsc(Long newsId);

    Discussion save(Discussion discussion);

    Optional<Discussion> findById(Long id);
}
