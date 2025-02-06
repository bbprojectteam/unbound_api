package com.badboys.unbound_gateway.config;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.server.HttpServer;
import reactor.netty.DisposableServer;

@Configuration
public class ServerConfig {

    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> httpRedirectCustomizer() {
        return factory -> factory.addServerCustomizers(httpServer -> {
            return httpServer.doOnBind(server -> {
                // 🔥 80 포트를 리디렉트 전용으로 설정
                DisposableServer disposableServer = HttpServer.create()
                        .port(80)
                        .route(routes -> routes
                                .get("/*", (request, response) -> {
                                    String host = request.requestHeaders().get("Host");
                                    if (host == null) {
                                        host = "freethebeast.duckdns.org";  // 기본값 설정 (필요시 변경)
                                    }

                                    // 🔥 원본 요청 헤더 복사
                                    request.requestHeaders().forEach(entry -> {
                                        response.header(entry.getKey().toString(), entry.getValue().toString());
                                    });

                                    // 🔥 307 리디렉트 적용 (POST → POST 유지, 헤더 유지)
                                    return response.status(HttpResponseStatus.TEMPORARY_REDIRECT)
                                            .header("Location", "https://" + host + request.uri())
                                            .send();
                                })
                        )
                        .bindNow();
            });
        });
    }
}
