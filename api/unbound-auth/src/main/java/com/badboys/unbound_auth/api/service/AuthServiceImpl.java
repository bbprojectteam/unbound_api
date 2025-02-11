package com.badboys.unbound_auth.api.service;

import com.badboys.unbound_auth.api.model.RequestUpdateFcmTokenDto;
import com.badboys.unbound_auth.api.model.TokenResponse;
import com.badboys.unbound_auth.api.entity.UserEntity;
import com.badboys.unbound_auth.api.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;

    private final TokenService tokenService;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }
    
    @Override
    public Long saveUser(UserEntity user) {

        if (!userRepository.existsByUid(user.getUid())) {
            UserEntity savedUser = userRepository.save(user); // 저장된 엔티티 반환
            return savedUser.getId(); // 저장된 PK 반환
        } else {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        }
    }

    @Override
    public HttpHeaders registerUser(String authorizationHeader) throws Exception {

        // Firebase에서 가져온 정보로 UserEntity 생성
        FirebaseToken decodedToken = tokenService.verifyTokenHeader(authorizationHeader);
        String uid = decodedToken.getUid();
        String username = decodedToken.getName() != null ? decodedToken.getName() : "Anonymous";

        UserEntity newUser = UserEntity.builder()
                .uid(uid) // Firebase UID 저장
                .username(username)
                .mmr(1000)
                .build();

        // 회원가입 처리
        Long userId = saveUser(newUser);

        // 토큰 생성 및 저장
        TokenResponse tokens = generateAndSaveTokens(userId);

        // 헤더 생성
        return getHttpHeaders(tokens.getAccessToken(), tokens.getRefreshToken());
    }

    @Override
    public HttpHeaders login(String authorizationHeader, RequestUpdateFcmTokenDto requestUpdateFcmTokenDto) throws Exception {
        // Firebase 토큰 검증
        FirebaseToken decodedToken = tokenService.verifyTokenHeader(authorizationHeader);
        String uid = decodedToken.getUid();

        // 사용자 조회
        UserEntity user = userRepository.findByUid(uid);
        
        if (user == null) {
            throw new IllegalArgumentException("등록되지 않은 사용자입니다.");
        }
        Long userId = user.getId();

        // 토큰 생성 및 저장
        TokenResponse tokens = generateAndSaveTokens(userId);
        // fcm 토큰 갱신
        tokenService.updateFcmToken(user, requestUpdateFcmTokenDto);

        // 헤더 생성
        return getHttpHeaders(tokens.getAccessToken(), tokens.getRefreshToken());
    }

    private TokenResponse generateAndSaveTokens(Long userId) {

        String accessToken = tokenService.generateToken(userId, "access");
        String refreshToken = tokenService.generateToken(userId, "refresh");

        tokenService.saveRefreshToken(userId, refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    private HttpHeaders getHttpHeaders(String accessToken, String refreshToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Refresh-Token", refreshToken);
        return headers;
    }
}
