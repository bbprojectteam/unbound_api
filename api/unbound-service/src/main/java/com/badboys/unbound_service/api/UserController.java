package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.service.UserService;
import com.badboys.unbound_service.model.RequestUpdateUserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
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
    public ResponseEntity<?> updateUser(@RequestHeader("X-User-Id") String userId, @RequestPart(value = "profileImageFile", required = false) MultipartFile profileImageFile) {

        try {
            userService.updateUserProfileImage(Long.valueOf(userId), profileImageFile);
            return ResponseEntity.ok(Map.of("message", "업데이트 성공"));
        } catch(RuntimeException  e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "이미지 업로드 실패"));
        }
    }
}
