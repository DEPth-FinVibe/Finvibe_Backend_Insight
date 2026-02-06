package finvibe.insight.modules.news.infra.persistence;

import finvibe.insight.modules.news.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsJpaRepository extends JpaRepository<News, Long> {
    boolean existsByTitle(String title);

    List<News> findAllByCreatedAtAfter(LocalDateTime createdAfter);

    List<News> findAllByCategoryIdAndPublishedAtBetweenOrderByPublishedAtDesc(
            Long categoryId,
            LocalDateTime start,
            LocalDateTime end);

    @Query("""
            select n.category.id as categoryId, count(n.id) as count
            from News n
            where n.publishedAt between :start and :end
              and n.category is not null
            group by n.category.id
            order by count desc
            """)
    List<CategoryCountProjection> countByCategoryIdForPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    interface CategoryCountProjection {
        Long getCategoryId();

        long getCount();
    }
}
