package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.service.MatchService;
import com.badboys.unbound_service.model.*;
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

        // 테스트용 임시로직
        for (int i = 1; i <= 5; i ++) {
            matchService.startMatch(Long.valueOf(i), requestMatchStartDto.getLimitRegionId());
        }

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
            ResponseMatchInfoDto responseMatchInfoDto = matchService.getMatchInfo(matchInfoId);
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

    @Operation(summary = "경기 시작", description = "경기 시작")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경기 시작"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/game/start")
    public ResponseEntity<?> gameStart(@RequestHeader("X-User-Id") String userId, @RequestBody RequestGameStartDto requestGameStartDto) {

        try {
            ResponseGameStartDto responseGameStartDto = matchService.startGame(requestGameStartDto);
            return ResponseEntity.ok(responseGameStartDto);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "서버 에러"));
        }
    }

    @Operation(summary = "경기 종료", description = "경기 결과를 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경기 종료 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/game/end")
    public ResponseEntity<Map<String, Object>> gameEnd(@RequestHeader("X-User-Id") String userId, @RequestBody RequestGameEndDto requestGameEndDto) {

        try {
            matchService.endGame(requestGameEndDto);

            return ResponseEntity.ok(Map.of("message", "경기 종료 처리 완료", "matchInfoId", requestGameEndDto.getMatchInfoId()));
        } catch (IllegalArgumentException e) {
            // 잘못된 요청 처리 (예: 존재하지 않는 팀/매치 등)
            return ResponseEntity.badRequest().body(Map.of("message", "요청이 잘못되었습니다", "error", e.getMessage()));
        } catch (Exception e) {
            // 예상하지 못한 서버 에러
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "서버 에러", "error", e.getMessage()));
        }
    }
}
