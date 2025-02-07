package com.badboys.unbound_gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced // `lb://`을 사용할 수 있도록 설정
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

