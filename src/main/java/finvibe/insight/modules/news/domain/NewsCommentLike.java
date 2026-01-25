package finvibe.insight.modules.news.domain;

import finvibe.insight.shared.domain.TimeStampedBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "news_comment_like", uniqueConstraints = @UniqueConstraint(name = "uk_news_comment_like_comment_user", columnNames = {
        "news_comment_id", "user_id" }))
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsCommentLike extends TimeStampedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "news_comment_id", nullable = false)
    private NewsComment comment;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    public static NewsCommentLike create(NewsComment comment, UUID userId) {
        return NewsCommentLike.builder()
                .comment(comment)
                .userId(userId)
                .build();
    }
}
