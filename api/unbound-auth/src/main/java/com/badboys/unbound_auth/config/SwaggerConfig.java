package com.badboys.unbound_auth.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("unbound-public")
                .pathsToMatch("/auth/**") // API 경로 지정
                .build();
    }
}
