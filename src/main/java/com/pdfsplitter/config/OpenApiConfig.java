package com.pdfsplitter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PDF Splitter API")
                        .version("1.0")
                        .description("REST API that splits PDF files every 50 pages into new files named {originalName}(pageStart-pageEnd).pdf"));
    }
}
