package finvibe.insight.modules.discussion.application;

import finvibe.insight.modules.discussion.application.port.in.DiscussionCommandUseCase;
import finvibe.insight.modules.discussion.application.port.out.DiscussionCommentRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionEventPublisher;
import finvibe.insight.modules.discussion.application.port.out.DiscussionLikeRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionRepository;
import finvibe.insight.modules.discussion.domain.Discussion;
import finvibe.insight.modules.discussion.domain.DiscussionComment;
import finvibe.insight.modules.discussion.domain.DiscussionLike;
import finvibe.insight.modules.discussion.domain.error.DiscussionErrorCode;
import finvibe.insight.modules.discussion.dto.DiscussionDto;
import finvibe.insight.shared.error.DomainException;
import finvibe.insight.shared.event.DiscussionCreatedEvent;
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
    private final DiscussionLikeRepository discussionLikeRepository;
    private final DiscussionEventPublisher discussionEventPublisher;

    @Override
    public DiscussionDto.Response addDiscussion(Long newsId, UUID userId, String content) {
        // DB 분리 환경에서는 newsId의 유효성 검증을 직접 하지 않거나 별도 통신(REST/gRPC)을 사용함.
        // 여기서는 newsId를 그대로 저장하고 이벤트를 발행하는 것에 집중.
        Discussion discussion = Discussion.create(newsId, userId, content);
        Discussion saved = discussionRepository.save(discussion);

        // 이벤트 발행
        discussionEventPublisher.publish(new DiscussionCreatedEvent(saved.getId(), newsId));

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
    public DiscussionDto.CommentResponse addCommentToDiscussion(Long discussionId, UUID userId, String content) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new DomainException(DiscussionErrorCode.DISCUSSION_NOT_FOUND));

        DiscussionComment comment = DiscussionComment.create(discussion, userId, content);
        DiscussionComment saved = discussionCommentRepository.save(comment);

        return new DiscussionDto.CommentResponse(saved);
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

        return new DiscussionDto.CommentResponse(updated);
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

    private DiscussionDto.Response mapToDiscussionResponse(Discussion discussion) {
        long likeCount = discussionLikeRepository.countByDiscussionId(discussion.getId());
        List<DiscussionComment> comments = discussionCommentRepository
                .findAllByDiscussionIdOrderByCreatedAtAsc(discussion.getId());

        List<DiscussionDto.CommentResponse> commentDtos = comments.stream()
                .map(DiscussionDto.CommentResponse::new)
                .toList();

        return new DiscussionDto.Response(discussion, likeCount, commentDtos);
    }
}
