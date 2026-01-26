package finvibe.insight.modules.news.application;

import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.domain.error.NewsErrorCode;
import finvibe.insight.shared.error.DomainException;
import finvibe.insight.shared.event.DiscussionCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DiscussionEventListener {

    private final NewsRepository newsRepository;

    @EventListener
    @Transactional
    public void handle(DiscussionCreatedEvent event) {
        if (event.getNewsId() == null) {
            return;
        }

        News news = newsRepository.findById(event.getNewsId())
                .orElseThrow(() -> new DomainException(NewsErrorCode.NEWS_NOT_FOUND));

        news.incrementDiscussionCount();
        newsRepository.save(news);
    }
}
