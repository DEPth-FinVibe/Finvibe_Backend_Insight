package finvibe.insight.modules.news.infra.persistence;

import finvibe.insight.modules.news.application.port.out.NewsLikeRepository;
import finvibe.insight.modules.news.domain.NewsLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NewsLikeRepositoryImpl implements NewsLikeRepository {

    private final NewsLikeJpaRepository newsLikeJpaRepository;

    @Override
    public long countByNewsId(Long newsId) {
        return newsLikeJpaRepository.countByNewsId(newsId);
    }

    @Override
    public NewsLike save(NewsLike like) {
        return newsLikeJpaRepository.save(like);
    }

    @Override
    public void delete(NewsLike like) {
        newsLikeJpaRepository.delete(like);
    }

    @Override
    public Optional<NewsLike> findByNewsIdAndUserId(Long newsId, UUID userId) {
        return newsLikeJpaRepository.findByNewsIdAndUserId(newsId, userId);
    }
}
