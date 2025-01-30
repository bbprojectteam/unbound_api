package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/match")
public class MatchController {

    private final MatchService matchService;

    @Autowired
    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @Operation(summary = "매칭 시작", description = "매칭 시작")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매칭 큐 입력 성공"),
            @ApiResponse(responseCode = "500", description = "매칭 요청 전송 실패")
    })
    @PostMapping("/start")
    public ResponseEntity<?> getMatchStart(@RequestHeader("X-User-Id") String userId, @RequestParam Long limitRegionId) {

        boolean isSuccess = matchService.getMatchStart(Long.valueOf(userId), limitRegionId);
        if (isSuccess) {
            return ResponseEntity.ok("매칭 요청 전송 성공");
        } else {
            return ResponseEntity.status(500).body("매칭 요청 전송 실패");
        }
    }
}
