package finvibe.insight.modules.news.presentation.external;

import finvibe.insight.boot.security.model.AuthenticatedUser;
import finvibe.insight.boot.security.model.Requester;
import finvibe.insight.modules.news.application.port.in.NewsCommandUseCase;
import finvibe.insight.modules.news.application.port.in.NewsQueryUseCase;
import finvibe.insight.modules.news.dto.NewsDto;
import finvibe.insight.modules.news.dto.NewsSortType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsQueryUseCase newsQueryUseCase;
    private final NewsCommandUseCase newsCommandUseCase;

    /**
     * 뉴스 목록을 조회합니다.
     * 
     * @param sortType 정렬 기준 (LATEST: 최신순, POPULAR: 인기순-좋아요순)
     */
    @GetMapping
    public List<NewsDto.Response> getNewsList(
            @RequestParam(value = "sort", defaultValue = "LATEST") NewsSortType sortType) {
        return newsQueryUseCase.findAllNewsSummary(sortType);
    }

    /**
     * 뉴스 상세 내용을 조회합니다.
     */
    @GetMapping("/{id}")
    public NewsDto.DetailResponse getNewsDetail(@PathVariable("id") Long id) {
        return newsQueryUseCase.findNewsById(id);
    }

    /**
     * 뉴스에 좋아요를 토글합니다. (로그인 사용자 필요)
     */
    @PostMapping("/{id}/like")
    public void toggleLike(@PathVariable("id") Long id, @AuthenticatedUser Requester requester) {
        newsCommandUseCase.toggleNewsLike(id, requester.getUuid());
    }
}
