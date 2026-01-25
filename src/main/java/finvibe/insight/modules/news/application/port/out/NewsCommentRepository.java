package finvibe.insight.modules.news.application.port.out;

import finvibe.insight.modules.news.domain.NewsComment;

import java.util.List;

public interface NewsCommentRepository {
    long countByNewsId(Long newsId);

    List<NewsComment> findAllByNewsIdOrderByCreatedAtAsc(Long newsId);

    List<NewsComment> findAllByParentIdOrderByCreatedAtAsc(Long parentId);
}
