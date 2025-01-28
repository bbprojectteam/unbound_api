package com.badboys.unbound_service.api;

import com.badboys.unbound_service.model.RequestMatchDto;
import com.badboys.unbound_service.model.ResponseUserInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/match")
public class MatchController {

    @Operation(summary = "매칭 시작", description = "매칭 시작")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매칭 큐 입력 성공"),
            @ApiResponse(responseCode = "500", description = "매칭 시작 실패")
    })
    @PostMapping("/start")
    public ResponseEntity<?> getMatchStart(@RequestHeader("X-User-Id") String userId, @RequestBody RequestMatchDto requestMatchDto) {


        return ResponseEntity.ok("매칭 시작");
    }
}
