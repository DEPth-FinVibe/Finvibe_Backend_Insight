package finvibe.insight.modules.discussion.presentation.external;

import finvibe.insight.modules.discussion.application.port.in.DiscussionCommandUseCase;
import finvibe.insight.modules.discussion.application.port.in.DiscussionQueryUseCase;
import finvibe.insight.modules.discussion.dto.DiscussionDto;
import finvibe.insight.modules.discussion.dto.DiscussionSortType;
import finvibe.insight.boot.security.model.AuthenticatedUser;
import finvibe.insight.boot.security.model.Requester;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/discussions")
@RequiredArgsConstructor
@Tag(name = "토론", description = "토론 스레드")
public class DiscussionController {

    private final DiscussionQueryUseCase discussionQueryUseCase;
    private final DiscussionCommandUseCase discussionCommandUseCase;

    /**
     * 전체 토론 목록을 조회합니다.
     *
     * @param sortType 정렬 기준 (LATEST, POPULAR)
     */
    @GetMapping
    @Operation(
            summary = "토론 목록 조회",
            description = "LATEST 또는 POPULAR 기준으로 토론 목록을 반환합니다."
    )
    public List<DiscussionDto.Response> getDiscussions(
            @Parameter(description = "정렬 기준 (LATEST 또는 POPULAR)")
            @RequestParam(value = "sort", defaultValue = "LATEST") DiscussionSortType sortType) {
        return discussionQueryUseCase.findAll(sortType);
    }

    /**
     * 새로운 토론을 작성합니다.
     */
    @PostMapping
    @Operation(
            summary = "토론 작성",
            description = "새 토론 스레드를 생성합니다."
    )
    public DiscussionDto.Response createDiscussion(
            @Parameter(description = "토론이 연결된 뉴스 ID")
            @RequestParam("newsId") Long newsId,
            @Parameter(hidden = true)
            @AuthenticatedUser Requester requester,
            @Parameter(description = "토론 내용")
            @RequestParam("content") String content) {
        return discussionCommandUseCase.addDiscussion(newsId, requester.getUuid(), content);
    }

    /**
     * 토론을 삭제합니다.
     */
    @DeleteMapping("/{discussionId}")
    @Operation(
            summary = "토론 삭제",
            description = "작성자인 경우 토론 스레드를 삭제합니다."
    )
    public void deleteDiscussion(
            @Parameter(description = "토론 ID")
            @PathVariable("discussionId") Long discussionId,
            @Parameter(hidden = true)
            @AuthenticatedUser Requester requester) {
        discussionCommandUseCase.deleteDiscussion(discussionId, requester.getUuid());
    }

    /**
     * 토론 좋아요를 토글합니다.
     */
    @PostMapping("/{discussionId}/like")
    @Operation(
            summary = "토론 좋아요 토글",
            description = "해당 사용자에 대해 좋아요를 토글합니다."
    )
    public void toggleLike(
            @Parameter(description = "토론 ID")
            @PathVariable("discussionId") Long discussionId,
            @Parameter(hidden = true)
            @AuthenticatedUser Requester requester) {
        discussionCommandUseCase.toggleDiscussionLike(discussionId, requester.getUuid());
    }
}
