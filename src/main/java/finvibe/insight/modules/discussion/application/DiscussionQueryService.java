package finvibe.insight.modules.discussion.application;

import finvibe.insight.modules.discussion.application.port.in.DiscussionQueryUseCase;
import finvibe.insight.modules.discussion.application.port.out.DiscussionCommentRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionLikeRepository;
import finvibe.insight.modules.discussion.application.port.out.DiscussionRepository;
import finvibe.insight.modules.discussion.domain.Discussion;
import finvibe.insight.modules.discussion.domain.DiscussionComment;
import finvibe.insight.modules.discussion.dto.DiscussionDto;
import finvibe.insight.modules.discussion.dto.DiscussionSortType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscussionQueryService implements DiscussionQueryUseCase {

    private final DiscussionRepository discussionRepository;
    private final DiscussionCommentRepository discussionCommentRepository;
    private final DiscussionLikeRepository discussionLikeRepository;

    @Override
    public long countByNewsId(Long newsId) {
        return discussionRepository.countByNewsId(newsId);
    }

    @Override
    public java.util.Map<Long, Long> countByNewsIds(java.util.List<Long> newsIds) {
        return discussionRepository.countByNewsIds(newsIds);
    }

    @Override
    public List<DiscussionDto.Response> findAllByNewsId(Long newsId, DiscussionSortType sortType) {
        List<DiscussionDto.Response> responses = discussionRepository
                .findAllByNewsIdOrderByCreatedAtAsc(newsId).stream()
                .map(this::mapToDiscussionResponse)
                .toList();

        if (sortType == DiscussionSortType.POPULAR) {
            return responses.stream()
                    .sorted(Comparator.comparingLong(DiscussionDto.Response::getLikeCount).reversed())
                    .toList();
        }

        return responses.stream()
                .sorted(Comparator.comparing(DiscussionDto.Response::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public List<DiscussionDto.Response> findAll(DiscussionSortType sortType) {
        List<Discussion> discussions;

        if (sortType == DiscussionSortType.LATEST) {
            // 최신순
            discussions = discussionRepository.findAllOrderByCreatedAtDesc();
            return discussions.stream()
                    .map(this::mapToDiscussionResponse)
                    .toList();
        } else {
            // 좋아요순
            discussions = discussionRepository.findAll();
            return discussions.stream()
                    .map(this::mapToDiscussionResponse)
                    .sorted(Comparator.comparingLong(DiscussionDto.Response::getLikeCount).reversed())
                    .toList();
        }
    }

    @Override
    public List<DiscussionDto.CommentResponse> findCommentsByDiscussionId(Long discussionId) {
        return discussionCommentRepository.findAllByDiscussionIdOrderByCreatedAtAsc(discussionId).stream()
                .map(comment -> new DiscussionDto.CommentResponse(comment, 0))
                .toList();
    }

    private DiscussionDto.Response mapToDiscussionResponse(Discussion discussion) {
        long likeCount = discussionLikeRepository.countByDiscussionId(discussion.getId());
        List<DiscussionComment> comments = discussionCommentRepository
                .findAllByDiscussionIdOrderByCreatedAtAsc(discussion.getId());

        List<DiscussionDto.CommentResponse> commentDtos = comments.stream()
                .map(comment -> new DiscussionDto.CommentResponse(comment, 0))
                .toList();

        return new DiscussionDto.Response(discussion, likeCount, commentDtos);
    }
}
