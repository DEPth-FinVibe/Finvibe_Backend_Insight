package finvibe.insight.boot.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Finvibe Insight API 문서",
                description = "Insight 서비스의 API 문서입니다.",
                version = "v1"
        )
)
public class SwaggerConfig {
}
