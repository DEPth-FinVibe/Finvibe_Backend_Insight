package finvibe.insight.modules.news.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NewsTest {

    @Test
    @DisplayName("discussion count increments and never goes below zero")
    void discussionCountBounds() {
        News news = News.create(
                "title",
                "content",
                "analysis",
                EconomicSignal.NEUTRAL,
                NewsKeyword.ETF,
                null,
                null,
                LocalDateTime.now(),
                "NAVER");

        news.incrementDiscussionCount();
        news.incrementDiscussionCount();
        assertThat(news.getDiscussionCount()).isEqualTo(2);

        news.decrementDiscussionCount();
        news.decrementDiscussionCount();
        news.decrementDiscussionCount();
        assertThat(news.getDiscussionCount()).isZero();
    }

    @Test
    @DisplayName("syncDiscussionCount replaces current count")
    void syncDiscussionCount() {
        News news = News.create(
                "title",
                "content",
                "analysis",
                EconomicSignal.NEUTRAL,
                NewsKeyword.ETF,
                null,
                null,
                LocalDateTime.now(),
                "NAVER");

        news.syncDiscussionCount(42);
        assertThat(news.getDiscussionCount()).isEqualTo(42);
    }
}
