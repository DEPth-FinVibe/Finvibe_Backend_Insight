package finvibe.insight.modules.news.application.port.out;

import finvibe.insight.modules.news.domain.NewsCommentLike;

import java.util.Optional;
import java.util.UUID;

public interface NewsCommentLikeRepository {
    NewsCommentLike save(NewsCommentLike like);

    void delete(NewsCommentLike like);

    Optional<NewsCommentLike> findByCommentIdAndUserId(Long commentId, UUID userId);
}
