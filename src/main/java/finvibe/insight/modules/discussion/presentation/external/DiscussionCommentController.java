package finvibe.insight.modules.discussion.presentation.external;

import finvibe.insight.boot.security.model.AuthenticatedUser;
import finvibe.insight.boot.security.model.Requester;
import finvibe.insight.modules.discussion.application.port.in.CommentCommandUseCase;
import finvibe.insight.modules.discussion.application.port.in.DiscussionQueryUseCase;
import finvibe.insight.modules.discussion.dto.DiscussionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
@Tag(name = "Discussion Comments", description = "Discussion comment operations")
public class DiscussionCommentController {

    private final DiscussionQueryUseCase discussionQueryUseCase;
    private final CommentCommandUseCase commentCommandUseCase;

    /**
     * 특정 토론의 댓글 목록을 조회합니다.
     */
    @GetMapping("/{discussionId}/comments")
    @Operation(
            summary = "List comments",
            description = "Returns comments for the given discussion."
    )
    public List<DiscussionDto.CommentResponse> getComments(@PathVariable("discussionId") Long discussionId) {
        return discussionQueryUseCase.findCommentsByDiscussionId(discussionId);
    }

    /**
     * 댓글을 작성합니다.
     */
    @PostMapping("/{discussionId}/comments")
    @Operation(
            summary = "Add comment",
            description = "Creates a comment for the authenticated user."
    )
    public DiscussionDto.CommentResponse addComment(
            @Parameter(description = "Discussion id")
            @PathVariable("discussionId") Long discussionId,
            @Parameter(hidden = true)
            @AuthenticatedUser Requester requester,
            @Parameter(description = "Comment content")
            @RequestParam("content") String content) {
        return commentCommandUseCase.addComment(discussionId, requester.getUuid(), content);
    }

    /**
     * 댓글을 삭제합니다.
     */
    @DeleteMapping("/comments/{commentId}")
    @Operation(
            summary = "Delete comment",
            description = "Deletes a comment for the authenticated user."
    )
    public void deleteComment(
            @Parameter(description = "Comment id")
            @PathVariable("commentId") Long commentId,
            @Parameter(hidden = true)
            @AuthenticatedUser Requester requester) {
        commentCommandUseCase.deleteComment(commentId, requester.getUuid());
    }

    /**
     * 댓글 좋아요를 토글합니다.
     */
    @PostMapping("/comments/{commentId}/like")
    @Operation(
            summary = "Toggle comment like",
            description = "Toggles like for the authenticated user."
    )
    public void toggleCommentLike(
            @Parameter(description = "Comment id")
            @PathVariable("commentId") Long commentId,
            @Parameter(hidden = true)
            @AuthenticatedUser Requester requester) {
        commentCommandUseCase.toggleCommentLike(commentId, requester.getUuid());
    }
}
