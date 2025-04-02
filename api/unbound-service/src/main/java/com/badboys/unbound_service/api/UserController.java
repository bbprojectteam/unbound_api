package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.service.MatchService;
import com.badboys.unbound_service.api.service.UserService;
import com.badboys.unbound_service.model.MatchHistoryDto;
import com.badboys.unbound_service.model.RequestUpdateUserDto;
import com.badboys.unbound_service.model.ResponseUserInfoDto;
import com.badboys.unbound_service.model.UserInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final MatchService matchService;

    @Autowired
    public UserController(UserService userService, MatchService matchService) {
        this.userService = userService;
        this.matchService = matchService;
    }

    @Operation(summary = "유저정보 업데이트", description = "유저정보 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @ApiResponse(responseCode = "404", description = "지역 에러")
    })
    @GetMapping("/my/info")
    public ResponseEntity<?> myInfo(@RequestHeader("X-User-Id") String userId) {

        try {
            UserInfoDto userInfoDto = userService.getUserInfo(Long.valueOf(userId));
            List<MatchHistoryDto> matchHistoryDtoList = matchService.getUserMatchHistoryList(Long.valueOf(userId));

            ResponseUserInfoDto responseUserInfoDto = new ResponseUserInfoDto(userInfoDto, matchHistoryDtoList);

            return ResponseEntity.ok(responseUserInfoDto);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "유저정보 업데이트", description = "유저정보 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @ApiResponse(responseCode = "404", description = "지역 에러")
    })
    @PostMapping("/update/info")
    public ResponseEntity<?> updateUser(@RequestHeader("X-User-Id") String userId, @RequestBody RequestUpdateUserDto requestUpdateUserDto) {

        try {
            userService.updateUser(Long.valueOf(userId), requestUpdateUserDto);
            return ResponseEntity.ok(Map.of("message", "업데이트 성공"));
        } catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "유효하지 않은 지역 값"));
        }
    }

    @Operation(summary = "유저 프로필 사진 업데이트", description = "유저정보 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @ApiResponse(responseCode = "500", description = "이미지 업로드 에러")
    })
    @PostMapping("/update/profileImage")
    public ResponseEntity<?> updateUser(@RequestHeader("X-User-Id") String userId, @RequestPart(value = "profileImageFile") MultipartFile profileImageFile) {

        try {
            userService.updateUserProfileImage(Long.valueOf(userId), profileImageFile);
            return ResponseEntity.ok(Map.of("message", "업데이트 성공"));
        } catch(RuntimeException  e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "이미지 업로드 실패"));
        }
    }

    @Operation(summary = "다른 유저 정보 조회", description = "유저 정보")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저 없음")
    })
    @GetMapping("/{targetUserId}/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("X-User-Id") String userId, @PathVariable Long targetUserId) {

        try {
            UserInfoDto userInfoDto = userService.getUserInfo(Long.valueOf(targetUserId));
            List<MatchHistoryDto> matchHistoryDtoList = matchService.getUserMatchHistoryList(Long.valueOf(targetUserId));

            ResponseUserInfoDto responseUserInfoDto = new ResponseUserInfoDto(userInfoDto, matchHistoryDtoList);

            return ResponseEntity.ok(responseUserInfoDto);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}
