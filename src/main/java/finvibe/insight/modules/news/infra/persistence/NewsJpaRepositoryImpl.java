package finvibe.insight.modules.news.infra.persistence;

import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.domain.News;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NewsJpaRepositoryImpl implements NewsRepository {

    private final NewsJpaRepository newsJpaRepository;

    @Override
    public News save(News news) {
        return newsJpaRepository.save(news);
    }

    @Override
    public List<News> findAll() {
        return newsJpaRepository.findAll();
    }

    @Override
    public Optional<News> findById(Long id) {
        return newsJpaRepository.findById(id);
    }
}
