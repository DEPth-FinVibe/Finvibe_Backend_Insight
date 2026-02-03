package finvibe.insight.modules.discussion.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DiscussionCommentTest {

    @Test
    @DisplayName("create should initialize comment fields")
    void createInitializesFields() {
        Discussion discussion = Discussion.create(1L, UUID.randomUUID(), "content");
        UUID userId = UUID.randomUUID();

        DiscussionComment comment = DiscussionComment.create(discussion, userId, "comment");

        assertThat(comment.getDiscussion()).isEqualTo(discussion);
        assertThat(comment.getUserId()).isEqualTo(userId);
        assertThat(comment.getContent()).isEqualTo("comment");
        assertThat(comment.isEdited()).isFalse();
    }

    @Test
    @DisplayName("updateContent marks comment as edited")
    void updateContentMarksEdited() {
        Discussion discussion = Discussion.create(1L, UUID.randomUUID(), "content");
        DiscussionComment comment = DiscussionComment.create(discussion, UUID.randomUUID(), "old");

        comment.updateContent("new");

        assertThat(comment.getContent()).isEqualTo("new");
        assertThat(comment.isEdited()).isTrue();
    }
}
