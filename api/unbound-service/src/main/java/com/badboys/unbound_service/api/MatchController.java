package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/match")
public class MatchController {

    private final MatchService matchService;

    @Autowired
    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @Operation(summary = "매칭 확인", description = "매칭 시작전 큐 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매칭 큐 확인"),
            @ApiResponse(responseCode = "500", description = "레디스 조회 에러")
    })
    @GetMapping("/confirm")
    public ResponseEntity<?> getMatchConfirm(@RequestHeader("X-User-Id") String userId) {

        try {
            boolean result = matchService.isMatchable(userId);
            return ResponseEntity.ok(Map.of("result", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("레디스 조회 에러");
        }
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

    @Operation(summary = "매칭 취소", description = "매칭 큐 취소")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매칭 큐 취소 완료"),
            @ApiResponse(responseCode = "500", description = "레디스 조회 에러")
    })
    @GetMapping("/cancle")
    public ResponseEntity<?> getMatchCancle(@RequestHeader("X-User-Id") String userId) {

        try {
            matchService.getMatchCancle(userId);
            return ResponseEntity.ok("매칭 취소 성공");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("레디스 조회 에러");
        }
    }
}
