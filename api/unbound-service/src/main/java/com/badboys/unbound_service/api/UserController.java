package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.service.UserService;
import com.badboys.unbound_service.model.ResponseUserInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @ApiResponse(responseCode = "204", description = "유저 정보 없음", content = @Content)
    })
    @GetMapping("/info")
    public ResponseEntity<ResponseUserInfoDto> getUserInfo(@RequestHeader("X-User-Id") String userId) {

        ResponseUserInfoDto responseUserInfoDto = userService.getUserInfo(Long.valueOf(userId));
        return ResponseEntity.ok(responseUserInfoDto);
    }
}
