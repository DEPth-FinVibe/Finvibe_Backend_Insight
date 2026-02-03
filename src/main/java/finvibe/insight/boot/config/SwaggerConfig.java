package finvibe.insight.boot.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Finvibe Insight API",
                description = "API documentation for the Insight service.",
                version = "v1"
        )
)
public class SwaggerConfig {
}
