package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.repository.RegionRepository;
import com.badboys.unbound_service.api.service.UserService;
import com.badboys.unbound_service.model.UserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/info")
    public ResponseEntity<String> getUserInfo(@RequestHeader("X-User-Id") String userId) {

//        UserInfo userInfo = userService.getUserInfo(Long.valueOf(userId));
        return ResponseEntity.status(HttpStatus.OK)
                .body("회원가입 성공");
    }
}
