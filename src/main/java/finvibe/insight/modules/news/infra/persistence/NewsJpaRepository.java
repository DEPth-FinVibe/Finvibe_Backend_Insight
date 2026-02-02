package finvibe.insight.modules.news.infra.persistence;

import finvibe.insight.modules.news.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsJpaRepository extends JpaRepository<News, Long> {
    boolean existsByTitle(String title);

    List<News> findAllByCreatedAtAfter(LocalDateTime createdAfter);
}
