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
public class NewsModuleScheduler {

    private final NewsCommandUseCase newsCommandUseCase;

    @Value("${news.crawler.cron:0 0 7 * * *}")
    private String newsSyncCron;

    /**
     * 지정된 스케줄에 맞춰 최신 뉴스를 수집하고 AI 분석을 수행합니다.
     */
    @Scheduled(cron = "${news.crawler.cron:0 0 7 * * *}")
    public void syncLatestNews() {
        log.info("Starting scheduled news collection and analysis...");
        newsCommandUseCase.syncLatestNews();
        log.info("Finished scheduled news collection and analysis.");
    }

    /**
     * 3시간마다 모든 뉴스의 토론 수를 최신화합니다.
     */
    @Scheduled(cron = "0 0 */3 * * *")
    public void syncDiscussionCounts() {
        log.info("Starting periodic discussion count synchronization...");
        newsCommandUseCase.syncAllDiscussionCounts();
        log.info("Finished periodic discussion count synchronization.");
    }
}
