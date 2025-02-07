package com.badboys.unbound_auth.api.service;

import com.badboys.unbound_auth.api.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TokenServiceImpl implements TokenService{

    private final String secretKey; // JWT 서명용 Secret Key
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7일
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public TokenServiceImpl(RedisTemplate<String, Object> redisTemplate, @Value("${jwt.secret}") String secretKey) {
        this.redisTemplate = redisTemplate;
        this.secretKey = secretKey;
    }

    @Override
    public String generateToken(Long userId, String type) {

        String token = null;
        if (type.equals("access")) {
            token = Jwts.builder()
                    .setSubject(String.valueOf(userId))
                    .setId(UUID.randomUUID().toString())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                    .signWith(SignatureAlgorithm.HS256, secretKey)
                    .compact();
        }
        else if (type.equals("refresh")) {
            token = Jwts.builder()
                    .setSubject(String.valueOf(userId))
                    .setId(UUID.randomUUID().toString())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                    .signWith(SignatureAlgorithm.HS256, secretKey)
                    .compact();
        }

        return token;
    }

    @Override
    public void saveRefreshToken(Long userId, String refreshToken) {

        redisTemplate.opsForValue().set(
                refreshToken, // 키: refreshToken
                String.valueOf(userId), // 값: userId
                REFRESH_TOKEN_EXPIRATION, // 만료 시간
                TimeUnit.DAYS // 단위
        );
    }

    // Refresh Token 조회
    @Override
    public Long getUserId(String refreshToken) {

        String userIdStr = (String) redisTemplate.opsForValue().get(refreshToken);
        return userIdStr != null ? Long.valueOf(userIdStr) : null;
    }

    // Refresh Token 삭제
    @Override
    public void deleteRefreshToken(String refreshToken) {

        redisTemplate.delete(refreshToken);
    }

    @Override
    public FirebaseToken verifyTokenHeader(String authorizationHeader) throws FirebaseAuthException {

        // Firebase 토큰 검증
        String idToken = authorizationHeader.replace("Bearer ", "");
        return FirebaseAuth.getInstance().verifyIdToken(idToken);
    }
}
