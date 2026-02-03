package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.out.NewsCrawler;
import finvibe.insight.modules.news.application.port.out.NewsDiscussionPort;
import finvibe.insight.modules.news.application.port.out.NewsLikeRepository;
import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.application.port.out.NewsSummarizer;
import finvibe.insight.modules.news.domain.EconomicSignal;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.domain.NewsKeyword;
import finvibe.insight.modules.news.domain.NewsLike;
import finvibe.insight.modules.news.domain.error.NewsErrorCode;
import finvibe.insight.shared.error.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsCommandServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsLikeRepository newsLikeRepository;

    @Mock
    private NewsCrawler newsCrawler;

    @Mock
    private NewsSummarizer newsSummarizer;

    @Mock
    private NewsDiscussionPort newsDiscussionPort;

    @InjectMocks
    private NewsCommandService newsCommandService;

    @Test
    @DisplayName("syncLatestNews saves only new titles and uses summarizer result")
    void syncLatestNewsSavesNewItems() {
        List<NewsCrawler.RawNewsData> rawData = List.of(
                new NewsCrawler.RawNewsData("title-a", "content-a"),
                new NewsCrawler.RawNewsData("title-b", "content-b")
        );
        when(newsCrawler.fetchLatestRawNews()).thenReturn(rawData);
        when(newsRepository.existsByTitle("title-a")).thenReturn(true);
        when(newsRepository.existsByTitle("title-b")).thenReturn(false);
        when(newsSummarizer.analyzeAndSummarize(anyString()))
                .thenReturn(new NewsSummarizer.AnalysisResult(
                        "summary", EconomicSignal.POSITIVE, NewsKeyword.AI));

        newsCommandService.syncLatestNews();

        ArgumentCaptor<News> captor = ArgumentCaptor.forClass(News.class);
        verify(newsRepository).save(captor.capture());
        News saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("title-b");
        assertThat(saved.getEconomicSignal()).isEqualTo(EconomicSignal.POSITIVE);
        assertThat(saved.getKeyword()).isEqualTo(NewsKeyword.AI);
    }

    @Test
    @DisplayName("syncAllDiscussionCounts only saves when count changes")
    void syncAllDiscussionCountsUpdatesChangedOnly() {
        News newsA = News.builder().id(1L).discussionCount(1).build();
        News newsB = News.builder().id(2L).discussionCount(3).build();
        when(newsRepository.findAll()).thenReturn(List.of(newsA, newsB));
        when(newsDiscussionPort.getDiscussionCounts(List.of(1L, 2L)))
                .thenReturn(Map.of(1L, 1L, 2L, 5L));

        newsCommandService.syncAllDiscussionCounts();

        ArgumentCaptor<News> captor = ArgumentCaptor.forClass(News.class);
        verify(newsRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(2L);
        assertThat(captor.getValue().getDiscussionCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("toggleNewsLike deletes existing like")
    void toggleNewsLikeDeletesExisting() {
        UUID userId = UUID.randomUUID();
        NewsLike existing = NewsLike.builder().id(10L).build();
        when(newsLikeRepository.findByNewsIdAndUserId(1L, userId))
                .thenReturn(Optional.of(existing));

        newsCommandService.toggleNewsLike(1L, userId);

        verify(newsLikeRepository).delete(existing);
        verify(newsLikeRepository, never()).save(any());
    }

    @Test
    @DisplayName("toggleNewsLike creates like when missing")
    void toggleNewsLikeCreatesWhenMissing() {
        UUID userId = UUID.randomUUID();
        News news = News.builder().id(1L).build();
        when(newsLikeRepository.findByNewsIdAndUserId(1L, userId))
                .thenReturn(Optional.empty());
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));

        newsCommandService.toggleNewsLike(1L, userId);

        ArgumentCaptor<NewsLike> captor = ArgumentCaptor.forClass(NewsLike.class);
        verify(newsLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getNews()).isEqualTo(news);
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("toggleNewsLike throws when news is missing")
    void toggleNewsLikeThrowsWhenMissingNews() {
        UUID userId = UUID.randomUUID();
        when(newsLikeRepository.findByNewsIdAndUserId(1L, userId))
                .thenReturn(Optional.empty());
        when(newsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsCommandService.toggleNewsLike(1L, userId))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException domainException = (DomainException) ex;
                    assertThat(domainException.getErrorCode()).isEqualTo(NewsErrorCode.NEWS_NOT_FOUND);
                });
    }
}
