package finvibe.insight.modules.news.application;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        when(newsDiscussionPort.getDiscussionCounts(anyList())).thenReturn(Map.of(1L, 0L, 2L, 0L));

        List<NewsDto.Response> results = newsQueryService.findAllNewsSummary(NewsSortType.POPULAR);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findAllNewsSummary sorts by publishedAt when LATEST")
    void findAllNewsSummaryLatest() {
        LocalDateTime older = LocalDateTime.now().minusDays(1);
        LocalDateTime newer = LocalDateTime.now();
        News oldNews = News.builder().id(1L).title("old").publishedAt(older).build();
        News newNews = News.builder().id(2L).title("new").publishedAt(newer).build();
        when(newsRepository.findAll()).thenReturn(List.of(oldNews, newNews));
        when(newsDiscussionPort.getDiscussionCounts(anyList())).thenReturn(Map.of(1L, 0L, 2L, 0L));

        List<NewsDto.Response> results = newsQueryService.findAllNewsSummary(NewsSortType.LATEST);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findAllNewsSummary places null publishedAt last when LATEST")
    void findAllNewsSummaryLatestWithNullPublishedAt() {
        LocalDateTime newer = LocalDateTime.now();
        News nullPublishedAt = News.builder().id(1L).title("null-date").build();
        News dated = News.builder().id(2L).title("dated").publishedAt(newer).build();
        when(newsRepository.findAll()).thenReturn(List.of(nullPublishedAt, dated));
        when(newsDiscussionPort.getDiscussionCounts(anyList())).thenReturn(Map.of(1L, 0L, 2L, 0L));

        List<NewsDto.Response> results = newsQueryService.findAllNewsSummary(NewsSortType.LATEST);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(2L);
        assertThat(results.get(1).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findAllNews uses DB-level publishedAt desc ordering when LATEST")
    void findAllNewsLatestUsesDbOrdering() {
        LocalDateTime newer = LocalDateTime.now();
        LocalDateTime older = newer.minusHours(1);
        News latest = News.builder().id(2L).title("latest").publishedAt(newer).build();
        News old = News.builder().id(1L).title("old").publishedAt(older).build();
        PageRequest pageable = PageRequest.of(0, 20);

        when(newsRepository.findAllOrderByPublishedAtDescIdDesc(any()))
                .thenReturn(new PageImpl<>(List.of(latest, old), pageable, 2));
        when(newsDiscussionPort.getDiscussionCounts(anyList())).thenReturn(Map.of(1L, 0L, 2L, 0L));

        var page = newsQueryService.findAllNews(NewsSortType.LATEST, pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getId()).isEqualTo(2L);
        verify(newsRepository).findAllOrderByPublishedAtDescIdDesc(any());
        verify(newsRepository, never()).findAll(any());
    }

    @Test
    @DisplayName("findNewsById returns detail response")
    void findNewsByIdReturnsDetail() {
        News news = News.builder()
                .id(1L)
                .title("title")
                .content("<p>content</p>")
                .contentText("content")
                .analysis("analysis")
                .economicSignal(EconomicSignal.NEUTRAL)
                .keyword(NewsKeyword.ETF)
                .build();
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(newsLikeRepository.countByNewsId(1L)).thenReturn(7L);
        when(newsDiscussionPort.getDiscussionCount(1L)).thenReturn(5L);

        NewsDto.DetailResponse response = newsQueryService.findNewsById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getContent()).isEqualTo("<p>content</p>");
        assertThat(response.getLikeCount()).isEqualTo(7L);
        assertThat(response.getDiscussionCount()).isEqualTo(5L);
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
    @DisplayName("findDailyTopKeywords aggregates from latest 30 news and fills fallback to 5")
    void findDailyTopKeywords() {
        News first = News.builder().id(1L).keyword(NewsKeyword.AI).build();
        News second = News.builder().id(2L).keyword(NewsKeyword.AI).build();
        News third = News.builder().id(3L).keyword(NewsKeyword.ETF).build();
        when(newsRepository.findAllOrderByPublishedAtDescIdDesc(any()))
                .thenReturn(new PageImpl<>(List.of(first, second, third), PageRequest.of(0, 30), 3));

        List<NewsDto.KeywordTrendResponse> results = newsQueryService.findDailyTopKeywords();

        assertThat(results).hasSize(5);
        assertThat(results.get(0).getKeyword()).isEqualTo(NewsKeyword.AI);
        assertThat(results.get(0).getCount()).isEqualTo(2);
        verify(newsRepository).findAllOrderByPublishedAtDescIdDesc(PageRequest.of(0, 30));
    }

    @Test
    @DisplayName("findDailyTopKeywords returns fallback keywords when all keywords are null")
    void findDailyTopKeywordsFallbackWhenNoKeywords() {
        News first = News.builder().id(1L).keyword(null).build();
        News second = News.builder().id(2L).keyword(null).build();
        when(newsRepository.findAllOrderByPublishedAtDescIdDesc(any()))
                .thenReturn(new PageImpl<>(List.of(first, second), PageRequest.of(0, 30), 2));

        List<NewsDto.KeywordTrendResponse> results = newsQueryService.findDailyTopKeywords();

        assertThat(results).hasSize(5);
        assertThat(results.get(0).getKeyword()).isEqualTo(NewsKeyword.AI);
        assertThat(results.get(0).getCount()).isEqualTo(0);
        assertThat(results.get(1).getKeyword()).isEqualTo(NewsKeyword.ETF);
        assertThat(results.get(2).getKeyword()).isEqualTo(NewsKeyword.SEMICONDUCTOR);
        assertThat(results.get(3).getKeyword()).isEqualTo(NewsKeyword.INFLATION);
        assertThat(results.get(4).getKeyword()).isEqualTo(NewsKeyword.RATE_CUT);
    }

    @Test
    @DisplayName("findDailyTopKeywords sorts ties by keyword name")
    void findDailyTopKeywordsTieBreakByKeywordName() {
        News first = News.builder().id(1L).keyword(NewsKeyword.RATE_CUT).build();
        News second = News.builder().id(2L).keyword(NewsKeyword.AI).build();
        when(newsRepository.findAllOrderByPublishedAtDescIdDesc(any()))
                .thenReturn(new PageImpl<>(List.of(first, second), PageRequest.of(0, 30), 2));

        List<NewsDto.KeywordTrendResponse> results = newsQueryService.findDailyTopKeywords();

        assertThat(results).hasSize(5);
        assertThat(results.get(0).getKeyword()).isEqualTo(NewsKeyword.AI);
        assertThat(results.get(0).getCount()).isEqualTo(1);
        assertThat(results.get(1).getKeyword()).isEqualTo(NewsKeyword.RATE_CUT);
        assertThat(results.get(1).getCount()).isEqualTo(1);
    }
}
