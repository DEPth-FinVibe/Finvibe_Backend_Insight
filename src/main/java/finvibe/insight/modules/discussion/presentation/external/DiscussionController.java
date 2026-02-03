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
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
@Tag(name = "Discussions", description = "Discussion threads")
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
            summary = "List discussions",
            description = "Returns discussions sorted by LATEST or POPULAR."
    )
    public List<DiscussionDto.Response> getDiscussions(
            @Parameter(description = "Sort order (LATEST or POPULAR)")
            @RequestParam(value = "sort", defaultValue = "LATEST") DiscussionSortType sortType) {
        return discussionQueryUseCase.findAll(sortType);
    }

    /**
     * 새로운 토론을 작성합니다.
     */
    @PostMapping
    @Operation(
            summary = "Create discussion",
            description = "Creates a new discussion thread."
    )
    public DiscussionDto.Response createDiscussion(
            @Parameter(description = "News id the discussion belongs to")
            @RequestParam("newsId") Long newsId,
            @Parameter(hidden = true)
            @AuthenticatedUser Requester requester,
            @Parameter(description = "Discussion content")
            @RequestParam("content") String content) {
        return discussionCommandUseCase.addDiscussion(newsId, requester.getUuid(), content);
    }

    /**
     * 토론을 삭제합니다.
     */
    @DeleteMapping("/{discussionId}")
    @Operation(
            summary = "Delete discussion",
            description = "Deletes a discussion thread if the user is the author."
    )
    public void deleteDiscussion(
            @Parameter(description = "Discussion id")
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
            summary = "Toggle discussion like",
            description = "Toggles like for the given user."
    )
    public void toggleLike(
            @Parameter(description = "Discussion id")
            @PathVariable("discussionId") Long discussionId,
            @Parameter(hidden = true)
            @AuthenticatedUser Requester requester) {
        discussionCommandUseCase.toggleDiscussionLike(discussionId, requester.getUuid());
    }
}
