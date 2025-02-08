package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.service.MatchService;
import com.badboys.unbound_service.api.service.UserService;
import com.badboys.unbound_service.model.ResponseMainInfoDto;
import com.badboys.unbound_service.model.UserInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/main")
public class MainController {

    private final UserService userService;

    private final MatchService matchService;

    @Autowired
    public MainController(UserService userService, MatchService matchService) {
        this.userService = userService;
        this.matchService = matchService;
    }

    @Operation(summary = "메인화면 조회", description = "메인 정보")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ResponseMainInfoDto.class))),
            @ApiResponse(responseCode = "404", description = "지역 정보 없음",
                    content = @Content(schema = @Schema(example = "{\"message\": \"지역 정보 없음\"}")))
    })
    @GetMapping("/info")
    public ResponseEntity<?> getMainInfo(@RequestHeader("X-User-Id") String userId) {

        UserInfoDto userInfoDto = userService.getUserInfo(Long.valueOf(userId));
        if (userInfoDto.getRegionNm() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "지역 정보 없음"));
        }

        ResponseMainInfoDto responseMainInfoDto = matchService.getMainMatchHistoryList(userInfoDto);
        responseMainInfoDto.setUserInfo(userInfoDto);
        return ResponseEntity.ok(responseMainInfoDto);
    }
}
