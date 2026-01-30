package finvibe.insight.modules.news.infra.persistence;

import finvibe.insight.modules.news.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsJpaRepository extends JpaRepository<News, Long> {
    boolean existsByTitle(String title);
}
