package finvibe.insight.modules.news.presentation.external;

import finvibe.insight.modules.news.application.port.in.ThemeQueryUseCase;
import finvibe.insight.modules.news.dto.ThemeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
@Tag(name = "테마", description = "오늘의 테마 분석 및 뉴스")
public class ThemeController {

    private final ThemeQueryUseCase themeQueryUseCase;

    @GetMapping("/today")
    @Operation(
            summary = "오늘의 테마 목록 조회",
            description = "오늘 선정된 테마 8개 목록을 반환합니다."
    )
    public List<ThemeDto.SummaryResponse> getTodayThemes() {
        return themeQueryUseCase.findTodayThemes();
    }

    @GetMapping("/today/{categoryId}")
    @Operation(
            summary = "오늘의 테마 상세 조회",
            description = "카테고리별 테마 분석과 관련 뉴스 목록을 반환합니다."
    )
    public ThemeDto.DetailResponse getTodayThemeDetail(
            @Parameter(description = "카테고리 ID")
            @PathVariable("categoryId") Long categoryId) {
        return themeQueryUseCase.findTodayThemeDetail(categoryId);
    }
}
