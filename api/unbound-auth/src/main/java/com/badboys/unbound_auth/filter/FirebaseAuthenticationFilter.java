package com.badboys.unbound_auth.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            try {
                String idToken = token.substring(7); // "Bearer " 이후의 토큰 값 추출
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken); // Firebase 토큰 검증

                // 인증 객체 생성 및 SecurityContext에 저장
                FirebaseAuthenticationToken authentication =
                        new FirebaseAuthenticationToken(decodedToken.getUid(), decodedToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // 토큰 검증 실패 시 401 반환
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid Firebase ID Token");
                return;
            }
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}