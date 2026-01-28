package finvibe.insight.modules.news.infra.persistence;

import finvibe.insight.modules.news.domain.NewsLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NewsLikeJpaRepository extends JpaRepository<NewsLike, Long> {
    long countByNewsId(Long newsId);

    Optional<NewsLike> findByNewsIdAndUserId(Long newsId, UUID userId);
}
