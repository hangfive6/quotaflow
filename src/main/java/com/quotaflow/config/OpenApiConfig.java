package com.quotaflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean OpenAPI api() { return new OpenAPI().info(new Info().title("QuotaFlow API").version("1.0.0").description("High-concurrency inventory reservation service")); }
}

