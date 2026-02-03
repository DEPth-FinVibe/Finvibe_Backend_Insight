package finvibe.insight.modules.discussion.application;

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
import finvibe.insight.shared.error.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscussionCommandServiceTest {

    @Mock
    private DiscussionRepository discussionRepository;

    @Mock
    private DiscussionCommentRepository discussionCommentRepository;

    @Mock
    private DiscussionCommentLikeRepository discussionCommentLikeRepository;

    @Mock
    private DiscussionLikeRepository discussionLikeRepository;

    @Mock
    private DiscussionEventPort discussionEventPort;

    @InjectMocks
    private DiscussionCommandService discussionCommandService;

    @Test
    @DisplayName("addDiscussion saves discussion and publishes event")
    void addDiscussionSavesAndPublishesEvent() {
        UUID userId = UUID.randomUUID();
        Discussion saved = Discussion.builder().id(1L).newsId(10L).userId(userId).content("content").build();
        when(discussionRepository.save(any())).thenReturn(saved);
        when(discussionLikeRepository.countByDiscussionId(1L)).thenReturn(0L);
        when(discussionCommentRepository.findAllByDiscussionIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        discussionCommandService.addDiscussion(10L, userId, "content");

        verify(discussionRepository).save(any(Discussion.class));
        verify(discussionEventPort).publishCreated(10L);
    }

    @Test
    @DisplayName("updateDiscussion updates content when author matches")
    void updateDiscussionUpdatesWhenAuthorMatches() {
        UUID userId = UUID.randomUUID();
        Discussion discussion = Discussion.builder().id(1L).userId(userId).content("old").build();
        when(discussionRepository.findById(1L)).thenReturn(Optional.of(discussion));
        when(discussionRepository.save(any())).thenReturn(discussion);
        when(discussionLikeRepository.countByDiscussionId(1L)).thenReturn(0L);
        when(discussionCommentRepository.findAllByDiscussionIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        discussionCommandService.updateDiscussion(1L, userId, "new");

        assertThat(discussion.getContent()).isEqualTo("new");
        assertThat(discussion.isEdited()).isTrue();
    }

    @Test
    @DisplayName("updateDiscussion throws when author mismatches")
    void updateDiscussionThrowsOnAuthorMismatch() {
        Discussion discussion = Discussion.builder().id(1L).userId(UUID.randomUUID()).build();
        when(discussionRepository.findById(1L)).thenReturn(Optional.of(discussion));

        assertThatThrownBy(() -> discussionCommandService.updateDiscussion(1L, UUID.randomUUID(), "new"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException domainException = (DomainException) ex;
                    assertThat(domainException.getErrorCode()).isEqualTo(DiscussionErrorCode.DISCUSSION_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("deleteDiscussion deletes and publishes event when author matches")
    void deleteDiscussionDeletesAndPublishesEvent() {
        UUID userId = UUID.randomUUID();
        Discussion discussion = Discussion.builder().id(1L).newsId(3L).userId(userId).build();
        when(discussionRepository.findById(1L)).thenReturn(Optional.of(discussion));

        discussionCommandService.deleteDiscussion(1L, userId);

        verify(discussionRepository).delete(discussion);
        verify(discussionEventPort).publishDeleted(3L);
    }

    @Test
    @DisplayName("addComment saves comment for discussion")
    void addCommentSavesComment() {
        UUID userId = UUID.randomUUID();
        Discussion discussion = Discussion.builder().id(1L).build();
        DiscussionComment saved = DiscussionComment.builder().id(2L).discussion(discussion).userId(userId).build();
        when(discussionRepository.findById(1L)).thenReturn(Optional.of(discussion));
        when(discussionCommentRepository.save(any())).thenReturn(saved);
        when(discussionCommentLikeRepository.countByCommentId(2L)).thenReturn(0L);

        discussionCommandService.addComment(1L, userId, "comment");

        verify(discussionCommentRepository).save(any(DiscussionComment.class));
    }

    @Test
    @DisplayName("updateComment updates when author matches")
    void updateCommentUpdatesWhenAuthorMatches() {
        UUID userId = UUID.randomUUID();
        DiscussionComment comment = DiscussionComment.builder().id(1L).userId(userId).content("old").build();
        when(discussionCommentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(discussionCommentRepository.save(any())).thenReturn(comment);
        when(discussionCommentLikeRepository.countByCommentId(1L)).thenReturn(0L);

        discussionCommandService.updateComment(1L, userId, "new");

        assertThat(comment.getContent()).isEqualTo("new");
        assertThat(comment.isEdited()).isTrue();
    }

    @Test
    @DisplayName("updateComment throws when author mismatches")
    void updateCommentThrowsOnAuthorMismatch() {
        DiscussionComment comment = DiscussionComment.builder().id(1L).userId(UUID.randomUUID()).build();
        when(discussionCommentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> discussionCommandService.updateComment(1L, UUID.randomUUID(), "new"))
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

        discussionCommandService.deleteComment(1L, userId);

        verify(discussionCommentRepository).delete(comment);
    }

    @Test
    @DisplayName("toggleDiscussionLike deletes existing like")
    void toggleDiscussionLikeDeletesExisting() {
        UUID userId = UUID.randomUUID();
        DiscussionLike existing = DiscussionLike.builder().id(1L).build();
        when(discussionLikeRepository.findByDiscussionIdAndUserId(1L, userId))
                .thenReturn(Optional.of(existing));

        discussionCommandService.toggleDiscussionLike(1L, userId);

        verify(discussionLikeRepository).delete(existing);
        verify(discussionLikeRepository, never()).save(any());
    }

    @Test
    @DisplayName("toggleDiscussionLike creates like when missing")
    void toggleDiscussionLikeCreatesWhenMissing() {
        UUID userId = UUID.randomUUID();
        Discussion discussion = Discussion.builder().id(1L).build();
        when(discussionLikeRepository.findByDiscussionIdAndUserId(1L, userId))
                .thenReturn(Optional.empty());
        when(discussionRepository.findById(1L)).thenReturn(Optional.of(discussion));

        discussionCommandService.toggleDiscussionLike(1L, userId);

        ArgumentCaptor<DiscussionLike> captor = ArgumentCaptor.forClass(DiscussionLike.class);
        verify(discussionLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getDiscussion()).isEqualTo(discussion);
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("toggleCommentLike deletes existing like")
    void toggleCommentLikeDeletesExisting() {
        UUID userId = UUID.randomUUID();
        DiscussionCommentLike existing = DiscussionCommentLike.builder().id(1L).build();
        when(discussionCommentLikeRepository.findByCommentIdAndUserId(1L, userId))
                .thenReturn(Optional.of(existing));

        discussionCommandService.toggleCommentLike(1L, userId);

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

        discussionCommandService.toggleCommentLike(1L, userId);

        ArgumentCaptor<DiscussionCommentLike> captor = ArgumentCaptor.forClass(DiscussionCommentLike.class);
        verify(discussionCommentLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getComment()).isEqualTo(comment);
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
    }
}
