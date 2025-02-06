package com.badboys.unbound_auth.api;

import com.badboys.unbound_auth.api.service.AuthService;
import com.badboys.unbound_auth.api.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final TokenService tokenService;

    @Autowired
    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @Operation(summary = "회원가입", description = "Firebase 토큰을 기반으로 사용자 회원가입")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            HttpHeaders responseHeader = authService.registerUser(authorizationHeader);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .headers(responseHeader)
                    .body("회원가입 성공");

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 Firebase 토큰입니다.");
        }
    }

    @Operation(summary = "로그인", description = "Firebase 토큰을 기반으로 사용자 로그인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "204", description = "미가입 사용자"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            HttpHeaders responseHeader = authService.login(authorizationHeader);

            return ResponseEntity.status(HttpStatus.OK)
                    .headers(responseHeader)
                    .body("로그인 성공");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("미가입 사용자 입니다");
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 Firebase 토큰입니다.");
        }
    }

    @Operation(summary = "재발급", description = "refresh 토큰 기반 access 토큰 재발급")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "500", description = "access 토큰 발급 실패")
    })
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshAccessToken(@RequestHeader("Refresh-Token") String refreshToken) {
        try {
            // Refresh Token 검증 및 userId 조회
            Long userId = tokenService.getUserId(refreshToken);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("유효하지 않은 Refresh Token입니다.");
            }

            // 새로운 Access Token 생성
            String newAccessToken = tokenService.generateToken(userId, "access");

            return ResponseEntity.ok(newAccessToken);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Access Token 재발급 실패: " + e.getMessage());
        }
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Refresh-Token") String refreshToken) {

        tokenService.deleteRefreshToken(refreshToken);
        return ResponseEntity.status(HttpStatus.OK)
                .body("로그아웃.");
    }


}
