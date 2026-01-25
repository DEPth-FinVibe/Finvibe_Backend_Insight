package finvibe.insight.modules.news.application.port.out;

public interface NewsLikeRepository {
    long countByNewsId(Long newsId);
}
