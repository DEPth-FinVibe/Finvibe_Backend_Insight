package finvibe.insight.modules.news.application.port.in;

import finvibe.insight.modules.news.dto.NewsDto;
import finvibe.insight.modules.news.dto.NewsSortType;

import java.util.List;

public interface NewsQueryUseCase {
    List<NewsDto.Response> findAllNewsSummary(NewsSortType sortType);

    NewsDto.DetailResponse findNewsById(Long id);
}
