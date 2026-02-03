package finvibe.insight.modules.discussion.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DiscussionTest {

    @Test
    @DisplayName("create should initialize discussion fields")
    void createInitializesFields() {
        UUID userId = UUID.randomUUID();
        Discussion discussion = Discussion.create(10L, userId, "content");

        assertThat(discussion.getNewsId()).isEqualTo(10L);
        assertThat(discussion.getUserId()).isEqualTo(userId);
        assertThat(discussion.getContent()).isEqualTo("content");
        assertThat(discussion.isEdited()).isFalse();
        assertThat(discussion.getComments()).isEmpty();
    }

    @Test
    @DisplayName("updateContent marks discussion as edited")
    void updateContentMarksEdited() {
        Discussion discussion = Discussion.create(1L, UUID.randomUUID(), "old");

        discussion.updateContent("new");

        assertThat(discussion.getContent()).isEqualTo("new");
        assertThat(discussion.isEdited()).isTrue();
    }
}
