package finvibe.insight.modules.discussion.presentation.external;

import finvibe.insight.boot.security.model.AuthenticatedUser;
import finvibe.insight.boot.security.model.Requester;
import finvibe.insight.modules.discussion.application.port.in.DiscussionCommandUseCase;
import finvibe.insight.modules.discussion.application.port.in.DiscussionQueryUseCase;
import finvibe.insight.modules.discussion.dto.DiscussionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionCommentController {

    private final DiscussionQueryUseCase discussionQueryUseCase;
    private final DiscussionCommandUseCase discussionCommandUseCase;

    /**
     * 특정 토론의 댓글 목록을 조회합니다.
     */
    @GetMapping("/{discussionId}/comments")
    public List<DiscussionDto.CommentResponse> getComments(@PathVariable("discussionId") Long discussionId) {
        return discussionQueryUseCase.findCommentsByDiscussionId(discussionId);
    }

    /**
     * 댓글을 작성합니다.
     */
    @PostMapping("/{discussionId}/comments")
    public DiscussionDto.CommentResponse addComment(
            @PathVariable("discussionId") Long discussionId,
            @AuthenticatedUser Requester requester,
            @RequestParam("content") String content) {
        return discussionCommandUseCase.addComment(discussionId, requester.getUuid(), content);
    }

    /**
     * 댓글을 삭제합니다.
     */
    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(
            @PathVariable("commentId") Long commentId,
            @AuthenticatedUser Requester requester) {
        discussionCommandUseCase.deleteComment(commentId, requester.getUuid());
    }

    /**
     * 댓글 좋아요를 토글합니다.
     */
    @PostMapping("/comments/{commentId}/like")
    public void toggleCommentLike(
            @PathVariable("commentId") Long commentId,
            @AuthenticatedUser Requester requester) {
        discussionCommandUseCase.toggleCommentLike(commentId, requester.getUuid());
    }
}
