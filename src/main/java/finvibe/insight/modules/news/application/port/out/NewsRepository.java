package finvibe.insight.modules.news.application.port.out;

import finvibe.insight.modules.news.domain.News;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NewsRepository {
    News save(News news);

    List<News> findAll();

    Optional<News> findById(Long id);

    boolean existsByTitle(String title);

    List<News> findAllByCreatedAtAfter(LocalDateTime createdAfter);

    List<News> findAllByCategoryIdAndPublishedAtBetweenOrderByPublishedAtDesc(
            Long categoryId,
            LocalDateTime start,
            LocalDateTime end);

    List<NewsCategoryCount> countByCategoryIdForPeriod(LocalDateTime start, LocalDateTime end);

    record NewsCategoryCount(Long categoryId, long count) {
    }
}
