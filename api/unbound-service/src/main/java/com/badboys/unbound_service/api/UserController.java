package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.service.UserService;
import com.badboys.unbound_service.model.RequestUpdateUserDto;
import com.badboys.unbound_service.model.ResponseUserInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "유저정보 조회", description = "내 정보 불러오기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "지역 정보 없음")
    })
    @GetMapping("/info")
    public ResponseEntity<ResponseUserInfoDto> getUserInfo(@RequestHeader("X-User-Id") String userId) {

        ResponseUserInfoDto responseUserInfoDto = userService.getUserInfo(Long.valueOf(userId));
        if (responseUserInfoDto.getRegionNm() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseUserInfoDto);
        }
        return ResponseEntity.ok(responseUserInfoDto);
    }

    @Operation(summary = "유저정보 업데이트", description = "유저정보 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @ApiResponse(responseCode = "404", description = "지역 에러")
    })
    @PostMapping("/update")
    public ResponseEntity<?> updateUser(@RequestHeader("X-User-Id") String userId, @RequestBody RequestUpdateUserDto requestUpdateUserDto) {

        try {
            userService.updateUser(Long.valueOf(userId), requestUpdateUserDto);
            return ResponseEntity.ok(Map.of("message", "업데이트 성공"));
        } catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "유효하지 않은 지역 값"));
        }
    }
}
