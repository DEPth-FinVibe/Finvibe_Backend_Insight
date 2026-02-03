package finvibe.insight.modules.discussion.presentation.internal;

import finvibe.insight.modules.discussion.application.port.in.DiscussionQueryUseCase;
import finvibe.insight.modules.discussion.dto.DiscussionDto;
import finvibe.insight.modules.discussion.dto.DiscussionSortType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/discussions")
@RequiredArgsConstructor
@Tag(name = "Discussion Internal", description = "Internal discussion APIs")
public class DiscussionInternalController {

    private final DiscussionQueryUseCase discussionQueryUseCase;

    /**
     * 특정 뉴스의 토론 목록을 조회합니다.
     *
     * @param newsId   뉴스 ID
     * @param sortType 정렬 기준 (LATEST, POPULAR)
     */
    @GetMapping
    @Operation(
            summary = "List discussions by news",
            description = "Returns discussions for a news item."
    )
    public List<DiscussionDto.Response> getDiscussions(
            @Parameter(description = "News id")
            @RequestParam("newsId") Long newsId,
            @Parameter(description = "Sort order (LATEST or POPULAR)")
            @RequestParam(value = "sort", defaultValue = "LATEST") DiscussionSortType sortType) {
        return discussionQueryUseCase.findAllByNewsId(newsId, sortType);
    }

    /**
     * 여러 뉴스의 토론 수를 벌크로 조회합니다.
     * 분산 환경에서 News 모듈이 HTTP로 호출하는 엔드포인트입니다.
     * 
     * @param newsIds 뉴스 ID 목록
     * @return Map<newsId, count>
     */
    @GetMapping("/counts")
    @Operation(
            summary = "Bulk discussion counts",
            description = "Returns a map of news id to discussion count."
    )
    public Map<Long, Long> getDiscussionCounts(@RequestParam("newsIds") List<Long> newsIds) {
        return discussionQueryUseCase.countByNewsIds(newsIds);
    }
}
