package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.in.NewsQueryUseCase;
import finvibe.insight.modules.news.application.port.out.NewsDiscussionPort;
import finvibe.insight.modules.news.application.port.out.NewsLikeRepository;
import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.domain.error.NewsErrorCode;
import finvibe.insight.modules.news.dto.NewsDto;
import finvibe.insight.modules.news.dto.NewsSortType;
import finvibe.insight.modules.discussion.dto.DiscussionDto;
import finvibe.insight.modules.discussion.dto.DiscussionSortType;
import finvibe.insight.shared.error.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsQueryService implements NewsQueryUseCase {

    private final NewsRepository newsRepository;
    private final NewsLikeRepository newsLikeRepository;
    private final NewsDiscussionPort newsDiscussionPort;

    @Override
    public List<NewsDto.Response> findAllNewsSummary(NewsSortType sortType) {
        List<News> newsList = newsRepository.findAll();

        if (sortType == NewsSortType.POPULAR) {
            // 인기순: 좋아요 개수로 정렬 (내림차순)
            Map<Long, Long> likeCountMap = newsList.stream()
                    .collect(Collectors.toMap(
                            News::getId,
                            news -> newsLikeRepository.countByNewsId(news.getId())));

            return newsList.stream()
                    .sorted(Comparator.comparing((News news) -> likeCountMap.get(news.getId())).reversed())
                    .map(NewsDto.Response::new)
                    .toList();
        } else {
            // 최신순: 생성일자로 정렬 (내림차순)
            return newsList.stream()
                    .sorted(Comparator.comparing(News::getCreatedAt).reversed())
                    .map(NewsDto.Response::new)
                    .toList();
        }
    }

    @Override
    public NewsDto.DetailResponse findNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new DomainException(NewsErrorCode.NEWS_NOT_FOUND));

        long likeCount = newsLikeRepository.countByNewsId(id);
        List<DiscussionDto.Response> discussions =
                newsDiscussionPort.getDiscussions(id, DiscussionSortType.LATEST);

        return new NewsDto.DetailResponse(news, likeCount, news.getDiscussionCount(), discussions);
    }
}
