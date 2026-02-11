package finvibe.insight.modules.discussion.application;

import finvibe.insight.modules.discussion.application.port.out.DiscussionCommentLikeRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionCommentRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionRepository;
import finvibe.insight.modules.discussion.domain.Discussion;
import finvibe.insight.modules.discussion.domain.DiscussionComment;
import finvibe.insight.modules.discussion.domain.DiscussionCommentLike;
import finvibe.insight.modules.discussion.domain.error.DiscussionErrorCode;
import finvibe.insight.shared.application.port.out.UserMetricEventPort;
import finvibe.insight.shared.dto.MetricEventType;
import finvibe.insight.shared.error.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentCommandServiceTest {

    @Mock
    private DiscussionRepository discussionRepository;

    @Mock
    private DiscussionCommentRepository discussionCommentRepository;

    @Mock
    private DiscussionCommentLikeRepository discussionCommentLikeRepository;

    @Mock
    private UserMetricEventPort userMetricEventPort;

    @InjectMocks
    private CommentCommandService commentCommandService;

    @Test
    @DisplayName("addComment saves comment for discussion")
    void addCommentSavesComment() {
        UUID userId = UUID.randomUUID();
        Discussion discussion = Discussion.builder().id(1L).build();
        DiscussionComment saved = DiscussionComment.builder().id(2L).discussion(discussion).userId(userId).build();
        when(discussionRepository.findById(1L)).thenReturn(Optional.of(discussion));
        when(discussionCommentRepository.save(any())).thenReturn(saved);
        when(discussionCommentLikeRepository.countByCommentId(2L)).thenReturn(0L);

        commentCommandService.addComment(1L, userId, "comment");

        verify(discussionCommentRepository).save(any(DiscussionComment.class));
        verify(userMetricEventPort).publish(eq(userId.toString()), eq(MetricEventType.DISCUSSION_COMMENT_COUNT), eq(1.0), any());
    }

    @Test
    @DisplayName("updateComment updates when author matches")
    void updateCommentUpdatesWhenAuthorMatches() {
        UUID userId = UUID.randomUUID();
        DiscussionComment comment = DiscussionComment.builder().id(1L).userId(userId).content("old").build();
        when(discussionCommentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(discussionCommentRepository.save(any())).thenReturn(comment);
        when(discussionCommentLikeRepository.countByCommentId(1L)).thenReturn(0L);

        commentCommandService.updateComment(1L, userId, "new");

        assertThat(comment.getContent()).isEqualTo("new");
        assertThat(comment.isEdited()).isTrue();
    }

    @Test
    @DisplayName("updateComment throws when author mismatches")
    void updateCommentThrowsOnAuthorMismatch() {
        DiscussionComment comment = DiscussionComment.builder().id(1L).userId(UUID.randomUUID()).build();
        when(discussionCommentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentCommandService.updateComment(1L, UUID.randomUUID(), "new"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException domainException = (DomainException) ex;
                    assertThat(domainException.getErrorCode()).isEqualTo(DiscussionErrorCode.DISCUSSION_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("deleteComment deletes when author matches")
    void deleteCommentDeletesWhenAuthorMatches() {
        UUID userId = UUID.randomUUID();
        DiscussionComment comment = DiscussionComment.builder().id(1L).userId(userId).build();
        when(discussionCommentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentCommandService.deleteComment(1L, userId);

        verify(discussionCommentLikeRepository).deleteByCommentId(1L);
        verify(discussionCommentRepository).delete(comment);
    }

    @Test
    @DisplayName("toggleCommentLike deletes existing like")
    void toggleCommentLikeDeletesExisting() {
        UUID userId = UUID.randomUUID();
        DiscussionCommentLike existing = DiscussionCommentLike.builder().id(1L).build();
        when(discussionCommentLikeRepository.findByCommentIdAndUserId(1L, userId))
                .thenReturn(Optional.of(existing));

        commentCommandService.toggleCommentLike(1L, userId);

        verify(discussionCommentLikeRepository).delete(existing);
        verify(discussionCommentLikeRepository, never()).save(any());
    }

    @Test
    @DisplayName("toggleCommentLike creates like when missing")
    void toggleCommentLikeCreatesWhenMissing() {
        UUID userId = UUID.randomUUID();
        DiscussionComment comment = DiscussionComment.builder().id(1L).build();
        when(discussionCommentLikeRepository.findByCommentIdAndUserId(1L, userId))
                .thenReturn(Optional.empty());
        when(discussionCommentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentCommandService.toggleCommentLike(1L, userId);

        ArgumentCaptor<DiscussionCommentLike> captor = ArgumentCaptor.forClass(DiscussionCommentLike.class);
        verify(discussionCommentLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getComment()).isEqualTo(comment);
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
    }
}
