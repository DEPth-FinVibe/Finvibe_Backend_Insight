package finvibe.insight.modules.discussion.application;

import finvibe.insight.modules.discussion.application.port.in.DiscussionCommandUseCase;
import finvibe.insight.modules.discussion.application.port.out.DiscussionCommentLikeRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionCommentRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionEventPort;
import finvibe.insight.modules.discussion.application.port.out.DiscussionLikeRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionRepository;
import finvibe.insight.modules.discussion.domain.Discussion;
import finvibe.insight.modules.discussion.domain.DiscussionComment;
import finvibe.insight.modules.discussion.domain.DiscussionCommentLike;
import finvibe.insight.modules.discussion.domain.DiscussionLike;
import finvibe.insight.modules.discussion.domain.error.DiscussionErrorCode;
import finvibe.insight.modules.discussion.application.port.out.DiscussionLikeRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionRepository;
import finvibe.insight.modules.discussion.domain.Discussion;
import finvibe.insight.modules.discussion.domain.DiscussionComment;
import finvibe.insight.modules.discussion.domain.DiscussionLike;
import finvibe.insight.modules.discussion.domain.error.DiscussionErrorCode;
import finvibe.insight.modules.discussion.dto.DiscussionDto;
import finvibe.insight.shared.error.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DiscussionCommandService implements DiscussionCommandUseCase {

    private final DiscussionRepository discussionRepository;
    private final DiscussionCommentRepository discussionCommentRepository;
    private final DiscussionCommentLikeRepository discussionCommentLikeRepository;
    private final DiscussionLikeRepository discussionLikeRepository;
    private final DiscussionEventPort discussionEventPort;

    @Override
    public DiscussionDto.Response addDiscussion(Long newsId, UUID userId, String content) {
        // DB 분리 환경에서는 newsId의 유효성 검증을 직접 하지 않거나 별도 통신(REST/gRPC)을 사용함.
        // 여기서는 newsId를 그대로 저장하고 이벤트를 발행하는 것에 집중.
        Discussion discussion = Discussion.create(newsId, userId, content);
        Discussion saved = discussionRepository.save(discussion);

        discussionEventPort.publishCreated(newsId);

        return mapToDiscussionResponse(saved);
    }

    @Override
    public DiscussionDto.Response updateDiscussion(Long discussionId, UUID userId, String content) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new DomainException(DiscussionErrorCode.DISCUSSION_NOT_FOUND));

        // 작성자만 수정 가능
        if (!discussion.getUserId().equals(userId)) {
            throw new DomainException(DiscussionErrorCode.DISCUSSION_NOT_FOUND);
        }

        discussion.updateContent(content);
        Discussion updated = discussionRepository.save(discussion);

        return mapToDiscussionResponse(updated);
    }

    @Override
    public void deleteDiscussion(Long discussionId, UUID userId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new DomainException(DiscussionErrorCode.DISCUSSION_NOT_FOUND));

        // 작성자만 삭제 가능
        if (!discussion.getUserId().equals(userId)) {
            throw new DomainException(DiscussionErrorCode.DISCUSSION_NOT_FOUND);
        }

        // cascade 설정으로 댓글도 자동 삭제됨
        discussionRepository.delete(discussion);

        discussionEventPort.publishDeleted(discussion.getNewsId());
    }

    @Override
    public DiscussionDto.CommentResponse addComment(Long discussionId, UUID userId, String content) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new DomainException(DiscussionErrorCode.DISCUSSION_NOT_FOUND));

        DiscussionComment comment = DiscussionComment.create(discussion, userId, content);
        DiscussionComment saved = discussionCommentRepository.save(comment);

        return mapToCommentResponse(saved);
    }

    @Override
    public DiscussionDto.CommentResponse updateComment(Long commentId, UUID userId, String content) {
        DiscussionComment comment = discussionCommentRepository.findById(commentId)
                .orElseThrow(() -> new DomainException(DiscussionErrorCode.DISCUSSION_NOT_FOUND));

        // 작성자만 수정 가능
        if (!comment.getUserId().equals(userId)) {
            throw new DomainException(DiscussionErrorCode.DISCUSSION_NOT_FOUND);
        }

        comment.updateContent(content);
        DiscussionComment updated = discussionCommentRepository.save(comment);

        return mapToCommentResponse(updated);
    }

    @Override
    public void deleteComment(Long commentId, UUID userId) {
        DiscussionComment comment = discussionCommentRepository.findById(commentId)
                .orElseThrow(() -> new DomainException(DiscussionErrorCode.DISCUSSION_NOT_FOUND));

        // 작성자만 삭제 가능
        if (!comment.getUserId().equals(userId)) {
            throw new DomainException(DiscussionErrorCode.DISCUSSION_NOT_FOUND);
        }

        discussionCommentRepository.delete(comment);
    }

    @Override
    public void toggleDiscussionLike(Long discussionId, UUID userId) {
        discussionLikeRepository.findByDiscussionIdAndUserId(discussionId, userId)
                .ifPresentOrElse(
                        discussionLikeRepository::delete,
                        () -> {
                            Discussion discussion = discussionRepository.findById(discussionId)
                                    .orElseThrow(() -> new DomainException(DiscussionErrorCode.DISCUSSION_NOT_FOUND));
                            discussionLikeRepository.save(DiscussionLike.create(discussion, userId));
                        });
    }

    @Override
    public void toggleCommentLike(Long commentId, UUID userId) {
        discussionCommentLikeRepository.findByCommentIdAndUserId(commentId, userId)
                .ifPresentOrElse(
                        discussionCommentLikeRepository::delete,
                        () -> {
                            DiscussionComment comment = discussionCommentRepository.findById(commentId)
                                    .orElseThrow(() -> new DomainException(DiscussionErrorCode.COMMENT_NOT_FOUND));
                            discussionCommentLikeRepository.save(DiscussionCommentLike.create(comment, userId));
                        });
    }

    private DiscussionDto.Response mapToDiscussionResponse(Discussion discussion) {
        long likeCount = discussionLikeRepository.countByDiscussionId(discussion.getId());
        List<DiscussionComment> comments = discussionCommentRepository
                .findAllByDiscussionIdOrderByCreatedAtAsc(discussion.getId());

        List<DiscussionDto.CommentResponse> commentDtos = comments.stream()
                .map(this::mapToCommentResponse)
                .toList();

        return new DiscussionDto.Response(discussion, likeCount, commentDtos);
    }

    private DiscussionDto.CommentResponse mapToCommentResponse(DiscussionComment comment) {
        long likeCount = discussionCommentLikeRepository.countByCommentId(comment.getId());
        return new DiscussionDto.CommentResponse(comment, likeCount);
    }
}
