package com.badboys.unbound_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    @Value("${jwt.secret}")
    private String secretKey;

    private final WebClient webClient;

    public JwtFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector())
                .baseUrl("lb://unbound-auth")
                .build();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
            String refreshToken = headers.getFirst("Refresh-Token"); // Refresh-Token 헤더

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Missing or invalid Authorization header");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7); // "Bearer " 제거
            try {
                // JWT 서명 및 만료 검증
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // USERID 추출
                String userId = claims.getSubject(); // "sub" 클레임 사용 (일반적으로 USERID를 여기에 저장)
                log.info("Successfully validated token for userId: {}", userId);

                // USERID를 요청 헤더에 추가
                exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .build();

                // 요청 계속 처리
                return chain.filter(exchange);

            } catch (ExpiredJwtException e) {
                log.info("ACCESS_TOKEN expired, attempting to refresh...");
                return refreshAuthToken(exchange, chain, refreshToken);
            } catch (JwtException e) {
                log.error("JWT validation failed: {}", e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private Mono<Void> refreshAuthToken(ServerWebExchange exchange, GatewayFilterChain chain, String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.error("Missing Refresh-Token header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Refresh Token 만료 확인
        Claims refreshTokenClaims;
        try {
            refreshTokenClaims = Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 시크릿 키 사용
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            Date expiration = refreshTokenClaims.getExpiration();
            if (expiration.before(new Date())) {
                log.error("Refresh-Token has expired");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        } catch (JwtException e) {
            log.error("Invalid Refresh-Token: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        log.info("Sending Refresh Token request to Auth Server: {}", webClient.mutate().build().toString());

        return webClient.post()
                .uri("/auth/refresh") // Refresh Token 검증 엔드포인트
                .header("Refresh-Token", refreshToken)
                .retrieve()
                .bodyToMono(String.class) // 새 ACCESS_TOKEN을 받는다고 가정
                .doOnSuccess(response -> log.info("Response from Auth Server: {}", response))
                .doOnError(error -> log.error("Error calling Auth Server: {}", error.getMessage()))
                .flatMap(newAccessToken -> {
                    // 새 ACCESS_TOKEN 검증 및 USERID 추출
                    Claims claims;
                    try {
                        claims = Jwts.parserBuilder()
                                .setSigningKey(secretKey)
                                .build()
                                .parseClaimsJws(newAccessToken)
                                .getBody();
                    } catch (JwtException e) {
                        log.error("New ACCESS_TOKEN validation failed: {}", e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    String userId = claims.getSubject();
                    log.info("New ACCESS_TOKEN issued for userId: {}", userId);

                    // 새로운 ACCESS_TOKEN과 USERID를 요청 헤더에 추가
                    exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .build();

                    exchange.getAttributes().put("accessTokenRefreshed", true);
                    exchange.getAttributes().put("newAccessToken", newAccessToken);

                    // 원래 요청 이어서 처리
                    return chain.filter(exchange);
                })
                .onErrorResume(error -> {
                    log.error("Failed to refresh ACCESS_TOKEN: {}", error.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    public static class Config {

    }
}