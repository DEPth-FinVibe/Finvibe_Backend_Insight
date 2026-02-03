package finvibe.insight.modules.discussion.application;

import finvibe.insight.modules.discussion.application.port.out.DiscussionCommentLikeRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionCommentRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionLikeRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionRepository;
import finvibe.insight.modules.discussion.domain.Discussion;
import finvibe.insight.modules.discussion.domain.DiscussionComment;
import finvibe.insight.modules.discussion.dto.DiscussionDto;
import finvibe.insight.modules.discussion.dto.DiscussionSortType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscussionQueryServiceTest {

    @Mock
    private DiscussionRepository discussionRepository;

    @Mock
    private DiscussionCommentRepository discussionCommentRepository;

    @Mock
    private DiscussionCommentLikeRepository discussionCommentLikeRepository;

    @Mock
    private DiscussionLikeRepository discussionLikeRepository;

    @InjectMocks
    private DiscussionQueryService discussionQueryService;

    @Test
    @DisplayName("findAllByNewsId sorts by like count when POPULAR")
    void findAllByNewsIdPopular() {
        Discussion first = Discussion.builder().id(1L).createdAt(LocalDateTime.now()).build();
        Discussion second = Discussion.builder().id(2L).createdAt(LocalDateTime.now()).build();
        when(discussionRepository.findAllByNewsIdOrderByCreatedAtAsc(10L))
                .thenReturn(List.of(first, second));
        when(discussionLikeRepository.countByDiscussionId(1L)).thenReturn(5L);
        when(discussionLikeRepository.countByDiscussionId(2L)).thenReturn(1L);
        when(discussionCommentRepository.findAllByDiscussionIdOrderByCreatedAtAsc(anyLong()))
                .thenReturn(List.of());

        List<DiscussionDto.Response> results = discussionQueryService.findAllByNewsId(10L, DiscussionSortType.POPULAR);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findAllByNewsId sorts by createdAt when LATEST")
    void findAllByNewsIdLatest() {
        LocalDateTime older = LocalDateTime.now().minusDays(1);
        LocalDateTime newer = LocalDateTime.now();
        Discussion oldDiscussion = Discussion.builder().id(1L).createdAt(older).build();
        Discussion newDiscussion = Discussion.builder().id(2L).createdAt(newer).build();
        when(discussionRepository.findAllByNewsIdOrderByCreatedAtAsc(10L))
                .thenReturn(List.of(oldDiscussion, newDiscussion));
        when(discussionLikeRepository.countByDiscussionId(anyLong())).thenReturn(0L);
        when(discussionCommentRepository.findAllByDiscussionIdOrderByCreatedAtAsc(anyLong()))
                .thenReturn(List.of());

        List<DiscussionDto.Response> results = discussionQueryService.findAllByNewsId(10L, DiscussionSortType.LATEST);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findAll sorts by like count when POPULAR")
    void findAllPopularSort() {
        Discussion first = Discussion.builder().id(1L).createdAt(LocalDateTime.now()).build();
        Discussion second = Discussion.builder().id(2L).createdAt(LocalDateTime.now()).build();
        when(discussionRepository.findAll()).thenReturn(List.of(first, second));
        when(discussionLikeRepository.countByDiscussionId(1L)).thenReturn(2L);
        when(discussionLikeRepository.countByDiscussionId(2L)).thenReturn(5L);
        when(discussionCommentRepository.findAllByDiscussionIdOrderByCreatedAtAsc(anyLong()))
                .thenReturn(List.of());

        List<DiscussionDto.Response> results = discussionQueryService.findAll(DiscussionSortType.POPULAR);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findAll uses latest ordering when LATEST")
    void findAllLatestSort() {
        Discussion oldDiscussion = Discussion.builder().id(1L).createdAt(LocalDateTime.now().minusDays(1)).build();
        Discussion newDiscussion = Discussion.builder().id(2L).createdAt(LocalDateTime.now()).build();
        when(discussionRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(newDiscussion, oldDiscussion));
        when(discussionLikeRepository.countByDiscussionId(anyLong())).thenReturn(0L);
        when(discussionCommentRepository.findAllByDiscussionIdOrderByCreatedAtAsc(anyLong()))
                .thenReturn(List.of());

        List<DiscussionDto.Response> results = discussionQueryService.findAll(DiscussionSortType.LATEST);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findCommentsByDiscussionId returns comment responses with like counts")
    void findCommentsByDiscussionIdReturnsComments() {
        Discussion discussion = Discussion.builder().id(1L).build();
        DiscussionComment comment = DiscussionComment.builder().id(10L).discussion(discussion).build();
        when(discussionCommentRepository.findAllByDiscussionIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(comment));
        when(discussionCommentLikeRepository.countByCommentId(10L)).thenReturn(7L);

        List<DiscussionDto.CommentResponse> results = discussionQueryService.findCommentsByDiscussionId(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLikeCount()).isEqualTo(7L);
    }
}
