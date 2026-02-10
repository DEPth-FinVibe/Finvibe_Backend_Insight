package finvibe.insight.modules.discussion.application;

import finvibe.insight.modules.discussion.application.port.out.DiscussionCommentLikeRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionCommentRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionEventPort;
import finvibe.insight.modules.discussion.application.port.out.DiscussionLikeRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionRepository;
import finvibe.insight.modules.discussion.domain.Discussion;
import finvibe.insight.modules.discussion.domain.DiscussionLike;
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

    @Mock
    private UserMetricEventPort userMetricEventPort;

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
        verify(userMetricEventPort).publish(eq(userId.toString()), eq(MetricEventType.NEWS_COMMENT_COUNT), eq(1.0), any());
    }

    @Test
    @DisplayName("addDiscussion with null newsId saves discussion without publishing event")
    void addDiscussionWithNullNewsIdSkipsEvent() {
        UUID userId = UUID.randomUUID();
        Discussion saved = Discussion.builder().id(1L).newsId(null).userId(userId).content("content").build();
        when(discussionRepository.save(any())).thenReturn(saved);
        when(discussionLikeRepository.countByDiscussionId(1L)).thenReturn(0L);
        when(discussionCommentRepository.findAllByDiscussionIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        discussionCommandService.addDiscussion(null, userId, "content");

        verify(discussionRepository).save(any(Discussion.class));
        verify(discussionEventPort, never()).publishCreated(any());
        verify(userMetricEventPort).publish(eq(userId.toString()), eq(MetricEventType.DISCUSSION_POST_COUNT), eq(1.0), any());
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
    @DisplayName("deleteDiscussion with null newsId deletes without publishing event")
    void deleteDiscussionWithNullNewsIdSkipsEvent() {
        UUID userId = UUID.randomUUID();
        Discussion discussion = Discussion.builder().id(1L).newsId(null).userId(userId).build();
        when(discussionRepository.findById(1L)).thenReturn(Optional.of(discussion));

        discussionCommandService.deleteDiscussion(1L, userId);

        verify(discussionRepository).delete(discussion);
        verify(discussionEventPort, never()).publishDeleted(any());
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
        verify(userMetricEventPort).publish(eq(userId.toString()), eq(MetricEventType.DISCUSSION_LIKE_COUNT), eq(-1.0), any());
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
        verify(userMetricEventPort).publish(eq(userId.toString()), eq(MetricEventType.DISCUSSION_LIKE_COUNT), eq(1.0), any());
    }

}
