package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.service.MatchService;
import com.badboys.unbound_service.model.RequestMatchStartDto;
import com.badboys.unbound_service.model.RequestUpdateCommentDto;
import com.badboys.unbound_service.model.ResponseMatchInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
            return ResponseEntity.status(500).body(Map.of("message", "레디스 조회 에러"));
        }
    }

    @Operation(summary = "매칭 시작", description = "매칭 시작")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매칭 큐 입력 성공"),
            @ApiResponse(responseCode = "500", description = "매칭 요청 전송 실패")
    })
    @PostMapping("/queue/start")
    public ResponseEntity<?> matchStart(@RequestHeader("X-User-Id") String userId, @RequestBody RequestMatchStartDto requestMatchStartDto) {

        boolean isSuccess = matchService.startMatch(Long.valueOf(userId), requestMatchStartDto.getLimitRegionId());
        if (isSuccess) {
            return ResponseEntity.ok(Map.of("message", "매칭 요청 전송 성공"));
        } else {
            return ResponseEntity.status(500).body(Map.of("message", "매칭 요청 전송 실패"));
        }
    }

    @Operation(summary = "매칭 취소", description = "매칭 큐 취소")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매칭 큐 취소 완료"),
            @ApiResponse(responseCode = "500", description = "레디스 조회 에러")
    })
    @PostMapping("/queue/cancle")
    public ResponseEntity<?> matchCancle(@RequestHeader("X-User-Id") String userId) {

        try {
            matchService.cancelMatch(userId);
            return ResponseEntity.ok(Map.of("message", "매칭 취소 성공"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "레디스 조회 에러"));
        }
    }

    @Operation(summary = "매칭 기록 정보 조회", description = "매칭 기록 상세 페이지")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ResponseMatchInfoDto.class))),
            @ApiResponse(responseCode = "404", description = "매치 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/info")
    public ResponseEntity<?> getMatchInfo(@RequestHeader("X-User-Id") String userId, @RequestParam Long matchInfoId) {

        try {
            ResponseMatchInfoDto responseMatchInfoDto = matchService.getMatchHistoryInfo(matchInfoId);
            return ResponseEntity.ok(responseMatchInfoDto);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "매치정보 없음"));
        }catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "서버 에러"));
        }
    }

    @Operation(summary = "댓글 업데이트", description = "댓글 입력, 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 업데이트 성공"),
            @ApiResponse(responseCode = "404", description = "매치 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/comment/update")
    public ResponseEntity<?> updateComment(@RequestHeader("X-User-Id") String userId, @RequestBody RequestUpdateCommentDto requestUpdateCommentDto) {

        try {
            matchService.updateComment(Long.valueOf(userId), requestUpdateCommentDto);
            return ResponseEntity.ok(Map.of("message", "댓글 업데이트 성공"));
        } catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message", "매치정보 없음"));
        }catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "서버 에러"));
        }
    }
}
