package finvibe.insight.modules.news.application;

import finvibe.insight.modules.discussion.dto.DiscussionDto;
import finvibe.insight.modules.discussion.dto.DiscussionSortType;
import finvibe.insight.modules.news.application.port.out.NewsDiscussionPort;
import finvibe.insight.modules.news.application.port.out.NewsLikeRepository;
import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.domain.EconomicSignal;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.domain.NewsKeyword;
import finvibe.insight.modules.news.dto.NewsDto;
import finvibe.insight.modules.news.dto.NewsSortType;
import finvibe.insight.modules.news.domain.error.NewsErrorCode;
import finvibe.insight.shared.error.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsQueryServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsLikeRepository newsLikeRepository;

    @Mock
    private NewsDiscussionPort newsDiscussionPort;

    @InjectMocks
    private NewsQueryService newsQueryService;

    @Test
    @DisplayName("findAllNewsSummary sorts by like count when POPULAR")
    void findAllNewsSummaryPopular() {
        News first = News.builder().id(1L).title("a").createdAt(LocalDateTime.now()).build();
        News second = News.builder().id(2L).title("b").createdAt(LocalDateTime.now()).build();
        when(newsRepository.findAll()).thenReturn(List.of(first, second));
        when(newsLikeRepository.countByNewsId(1L)).thenReturn(10L);
        when(newsLikeRepository.countByNewsId(2L)).thenReturn(3L);

        List<NewsDto.Response> results = newsQueryService.findAllNewsSummary(NewsSortType.POPULAR);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findAllNewsSummary sorts by createdAt when LATEST")
    void findAllNewsSummaryLatest() {
        LocalDateTime older = LocalDateTime.now().minusDays(1);
        LocalDateTime newer = LocalDateTime.now();
        News oldNews = News.builder().id(1L).title("old").createdAt(older).build();
        News newNews = News.builder().id(2L).title("new").createdAt(newer).build();
        when(newsRepository.findAll()).thenReturn(List.of(oldNews, newNews));

        List<NewsDto.Response> results = newsQueryService.findAllNewsSummary(NewsSortType.LATEST);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findAllNewsSummary places null createdAt last when LATEST")
    void findAllNewsSummaryLatestWithNullCreatedAt() {
        LocalDateTime newer = LocalDateTime.now();
        News nullCreatedAt = News.builder().id(1L).title("null-date").build();
        News dated = News.builder().id(2L).title("dated").createdAt(newer).build();
        when(newsRepository.findAll()).thenReturn(List.of(nullCreatedAt, dated));

        List<NewsDto.Response> results = newsQueryService.findAllNewsSummary(NewsSortType.LATEST);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(2L);
        assertThat(results.get(1).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findNewsById returns detail response with discussions")
    void findNewsByIdReturnsDetail() {
        News news = News.builder()
                .id(1L)
                .title("title")
                .content("content")
                .analysis("analysis")
                .economicSignal(EconomicSignal.NEUTRAL)
                .keyword(NewsKeyword.ETF)
                .build();
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(newsLikeRepository.countByNewsId(1L)).thenReturn(7L);
        when(newsDiscussionPort.getDiscussions(1L, DiscussionSortType.LATEST))
                .thenReturn(List.of());

        NewsDto.DetailResponse response = newsQueryService.findNewsById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLikeCount()).isEqualTo(7L);
        assertThat(response.getDiscussions()).isEmpty();
    }

    @Test
    @DisplayName("findNewsById throws when missing")
    void findNewsByIdThrowsWhenMissing() {
        when(newsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsQueryService.findNewsById(1L))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException domainException = (DomainException) ex;
                    assertThat(domainException.getErrorCode()).isEqualTo(NewsErrorCode.NEWS_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("findDailyTopKeywords aggregates and sorts keyword counts")
    void findDailyTopKeywords() {
        LocalDateTime now = LocalDateTime.now();
        News first = News.builder().id(1L).createdAt(now.minusHours(2)).keyword(NewsKeyword.AI).build();
        News second = News.builder().id(2L).createdAt(now.minusHours(3)).keyword(NewsKeyword.AI).build();
        News third = News.builder().id(3L).createdAt(now.minusHours(4)).keyword(NewsKeyword.ETF).build();
        when(newsRepository.findAllByCreatedAtAfter(any()))
                .thenReturn(List.of(first, second, third));

        List<NewsDto.KeywordTrendResponse> results = newsQueryService.findDailyTopKeywords();

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getKeyword()).isEqualTo(NewsKeyword.AI);
        assertThat(results.get(0).getCount()).isEqualTo(2);
    }
}
