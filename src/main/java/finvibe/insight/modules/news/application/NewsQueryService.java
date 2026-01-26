package finvibe.insight.modules.news.application;

import finvibe.insight.modules.discussion.application.port.in.DiscussionQueryUseCase;
import finvibe.insight.modules.discussion.dto.DiscussionDto;
import finvibe.insight.modules.news.application.port.in.NewsQueryUseCase;
import finvibe.insight.modules.news.application.port.out.NewsLikeRepository;
import finvibe.insight.modules.news.application.port.out.NewsRepository;
import finvibe.insight.modules.news.domain.News;
import finvibe.insight.modules.news.domain.error.NewsErrorCode;
import finvibe.insight.modules.news.dto.NewsDto;
import finvibe.insight.shared.error.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsQueryService implements NewsQueryUseCase {

        private final NewsRepository newsRepository;
        private final NewsLikeRepository newsLikeRepository;
        private final DiscussionQueryUseCase discussionQueryUseCase;

        @Override
        public List<NewsDto.Response> findAllNewsSummary() {
                return newsRepository.findAll().stream()
                                .map(NewsDto.Response::new)
                                .toList();
        }

        @Override
        public NewsDto.DetailResponse findNewsById(Long id) {
                News news = newsRepository.findById(id)
                                .orElseThrow(() -> new DomainException(NewsErrorCode.NEWS_NOT_FOUND));

                long likeCount = newsLikeRepository.countByNewsId(id);
                long discussionCount = discussionQueryUseCase.countByNewsId(id);
                List<DiscussionDto.Response> discussions = discussionQueryUseCase.findAllByNewsId(id);

                return new NewsDto.DetailResponse(news, likeCount, discussionCount, discussions);
        }
}
