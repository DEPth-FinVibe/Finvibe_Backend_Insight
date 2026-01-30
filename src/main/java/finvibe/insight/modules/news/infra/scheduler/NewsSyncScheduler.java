package finvibe.insight.modules.news.infra.scheduler;

import finvibe.insight.modules.news.application.port.in.NewsCommandUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsSyncScheduler {

    private final NewsCommandUseCase newsCommandUseCase;

    @Value("${news.crawler.cron:0 0 12 * * *}")
    private String cron;

    /**
     * 지정된 cron 스케줄에 맞춰 최신 뉴스를 동기화합니다.
     */
    @Scheduled(cron = "${news.crawler.cron:0 0 7 * * *}")
    public void syncLatestNews() {
        log.info("Starting scheduled news sync with cron: {}", cron);
        newsCommandUseCase.syncLatestNews();
        log.info("Finished scheduled news sync.");
    }
}
