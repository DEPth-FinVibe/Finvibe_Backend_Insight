package finvibe.insight.modules.news.application.port.out;

import finvibe.insight.modules.news.domain.NewsComment;

import java.util.List;
import java.util.Optional;

public interface NewsCommentRepository {
    long countByNewsId(Long newsId);

    List<NewsComment> findAllByNewsIdOrderByCreatedAtAsc(Long newsId);

    List<NewsComment> findAllByParentIdOrderByCreatedAtAsc(Long parentId);

    NewsComment save(NewsComment comment);

    Optional<NewsComment> findById(Long id);
}
