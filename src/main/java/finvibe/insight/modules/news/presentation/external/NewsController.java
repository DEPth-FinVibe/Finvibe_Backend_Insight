package finvibe.insight.modules.news.presentation.external;

import finvibe.insight.boot.security.model.AuthenticatedUser;
import finvibe.insight.boot.security.model.Requester;
import finvibe.insight.modules.news.application.port.in.NewsCommandUseCase;
import finvibe.insight.modules.news.application.port.in.NewsQueryUseCase;
import finvibe.insight.modules.news.dto.NewsDto;
import finvibe.insight.modules.news.dto.NewsSortType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Tag(name = "뉴스", description = "뉴스 조회 및 동작")
public class NewsController {

    private final NewsQueryUseCase newsQueryUseCase;
    private final NewsCommandUseCase newsCommandUseCase;

    /**
     * 뉴스 목록을 조회합니다.
     * 
     * @param sortType 정렬 기준 (LATEST: 최신순, POPULAR: 인기순-좋아요순)
     */
    @GetMapping
    @Operation(
            summary = "뉴스 목록 조회",
            description = "지정한 정렬 기준으로 뉴스 요약 목록을 반환합니다."
    )
    public List<NewsDto.Response> getNewsList(
            @Parameter(description = "정렬 기준 (LATEST 또는 POPULAR)")
            @RequestParam(value = "sort", defaultValue = "LATEST") NewsSortType sortType) {
        return newsQueryUseCase.findAllNewsSummary(sortType);
    }

    /**
     * 뉴스 상세 내용을 조회합니다.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "뉴스 상세 조회",
            description = "뉴스 상세, 좋아요, 토론 정보를 포함해 단건을 반환합니다."
    )
    public NewsDto.DetailResponse getNewsDetail(@PathVariable("id") Long id) {
        return newsQueryUseCase.findNewsById(id);
    }

    /**
     * 뉴스에 좋아요를 토글합니다. (로그인 사용자 필요)
     */
    @PostMapping("/{id}/like")
    @Operation(
            summary = "뉴스 좋아요 토글",
            description = "인증된 사용자에 대해 좋아요를 토글합니다."
    )
    public void toggleLike(@PathVariable("id") Long id, @AuthenticatedUser Requester requester) {
        newsCommandUseCase.toggleNewsLike(id, requester.getUuid());
    }

    /**
     * 하루 기준으로 가장 많이 등장한 키워드 5개를 조회합니다.
     */
    @GetMapping("/keywords/trending")
    @Operation(
            summary = "일간 키워드 트렌드 조회",
            description = "최근 1일 기준 상위 5개 키워드를 반환합니다."
    )
    public List<NewsDto.KeywordTrendResponse> getDailyKeywordTrends() {
        return newsQueryUseCase.findDailyTopKeywords();
    }
}
