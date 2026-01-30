package finvibe.insight.modules.news.infra.scheduler;

import finvibe.insight.modules.news.application.port.in.NewsCommandUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsDiscussionCountScheduler {

    private final NewsCommandUseCase newsCommandUseCase;

    /**
     * 3시간마다 모든 뉴스의 토론 수를 최신화합니다.
     * 비즈니스 로직은 Application 계층(NewsCommandService)에 있으며,
     * 스케줄러는 실행 시점에 해당 서비스를 호출하는 Adapter 역할만 수행합니다.
     */
    @Scheduled(cron = "0 0 */3 * * *")
    public void syncDiscussionCounts() {
        log.info("Starting synchronization of news discussion counts via scheduler...");
        newsCommandUseCase.syncAllDiscussionCounts();
        log.info("Finished synchronization of news discussion counts via scheduler.");
    }
}
