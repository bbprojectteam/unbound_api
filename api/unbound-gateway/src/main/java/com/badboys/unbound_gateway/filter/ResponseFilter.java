package com.badboys.unbound_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ResponseFilter extends AbstractGatewayFilterFactory<ResponseFilter.Config> {

    public ResponseFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Boolean tokenRefreshed = exchange.getAttribute("accessTokenRefreshed");
            String newAccessToken = exchange.getAttribute("newAccessToken");

            if (Boolean.TRUE.equals(tokenRefreshed) && newAccessToken != null) {
                exchange.getResponse().getHeaders().add("New-Access-Token", newAccessToken);
                log.info("Added new ACCESS_TOKEN to response header.");
            }
        }));
    }

    public static class Config {
        // 필요 시 설정 추가
    }
}

