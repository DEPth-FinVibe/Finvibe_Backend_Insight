package finvibe.insight.modules.discussion.presentation.external;

import finvibe.insight.modules.discussion.application.port.in.DiscussionCommandUseCase;
import finvibe.insight.modules.discussion.application.port.in.DiscussionQueryUseCase;
import finvibe.insight.modules.discussion.dto.DiscussionDto;
import finvibe.insight.modules.discussion.dto.DiscussionSortType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionQueryUseCase discussionQueryUseCase;
    private final DiscussionCommandUseCase discussionCommandUseCase;

    /**
     * 특정 뉴스의 토론 목록을 조회합니다.
     * 
     * @param newsId   뉴스 ID
     * @param sortType 정렬 기준 (LATEST, POPULAR)
     */
    @GetMapping
    public List<DiscussionDto.Response> getDiscussions(
            @RequestParam("newsId") Long newsId,
            @RequestParam(value = "sort", defaultValue = "LATEST") DiscussionSortType sortType) {
        return discussionQueryUseCase.findAllByNewsId(newsId, sortType);
    }

    /**
     * 새로운 토론을 작성합니다.
     */
    @PostMapping
    public DiscussionDto.Response createDiscussion(
            @RequestParam("newsId") Long newsId,
            @RequestParam("userId") UUID userId,
            @RequestParam("content") String content) {
        return discussionCommandUseCase.addDiscussion(newsId, userId, content);
    }

    /**
     * 토론을 삭제합니다.
     */
    @DeleteMapping("/{discussionId}")
    public void deleteDiscussion(
            @PathVariable("discussionId") Long discussionId,
            @RequestParam("userId") UUID userId) {
        discussionCommandUseCase.deleteDiscussion(discussionId, userId);
    }

    /**
     * 토론 좋아요를 토글합니다.
     */
    @PostMapping("/{discussionId}/like")
    public void toggleLike(
            @PathVariable("discussionId") Long discussionId,
            @RequestParam("userId") UUID userId) {
        discussionCommandUseCase.toggleDiscussionLike(discussionId, userId);
    }
}
