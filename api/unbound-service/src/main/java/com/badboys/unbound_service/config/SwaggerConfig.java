package com.badboys.unbound_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${host}")
    private String hostUrl;

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("unbound-public")
                .pathsToMatch("/**") // API 경로 지정
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Unbound API")
                        .version("1.0")
                        .description("Unbound 서비스의 API 문서입니다.")
                )
                .addServersItem(new Server().url(hostUrl).description("Gateway Server")); // ✅ Gateway URL 설정
    }
}
