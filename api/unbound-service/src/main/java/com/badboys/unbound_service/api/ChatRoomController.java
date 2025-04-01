package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.service.ChatRoomService;
import com.badboys.unbound_service.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chatRoom")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Autowired
    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @Operation(summary = "참여한 채팅방 목록 조회", description = "참여한 채팅방 목록")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChatRoomThumbnailDto.class))))
    @GetMapping("/list/joined")
    public ResponseEntity<?> getJoinedChatRoomList(@RequestHeader("X-User-Id") String userId) {

        List<ChatRoomThumbnailDto> chatRoomList = chatRoomService.getJoinedChatRoomList(Long.parseLong(userId));
        return ResponseEntity.ok(Map.of("chatRoomList", chatRoomList));
    }

    @Operation(summary = "참여한 채팅방 목록 조회", description = "참여한 채팅방 목록")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChatRoomThumbnailDto.class))))
    @GetMapping("/list/unJoined/{regionId}")
    public ResponseEntity<?> getUnJoinedChatRoomList(@RequestHeader("X-User-Id") String userId, @PathVariable Long regionId) {

        List<ChatRoomThumbnailDto> chatRoomList = chatRoomService.getUnJoinedChatRoomList(Long.parseLong(userId), regionId);
        return ResponseEntity.ok(Map.of("chatRoomList", chatRoomList));
    }

    @Operation(summary = "채팅방 정보 조회", description = "채팅방 정보")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음",
                    content = @Content(schema = @Schema(example = "{\"message\": \"채팅방을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버에러",
                    content = @Content(schema = @Schema(example = "{\"message\": \"서버에러\"}")))
    })
    @GetMapping("/{chatRoomId}/info")
    public ResponseEntity<?> getChatRoomInfo(@RequestHeader("X-User-Id") String userId, @PathVariable Long chatRoomId) {

        try {
            ResponseChatRoomInfoDto responseChatRoomInfoDto = chatRoomService.getChatRoomInfo(Long.parseLong(userId), chatRoomId);
            return ResponseEntity.ok(responseChatRoomInfoDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "채팅방을 찾을 수 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버에러"));
        }
    }

    @Operation(summary = "채팅방 정보 업데이트", description = "채팅방 정보 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업데이트 성공",
                    content = @Content(schema = @Schema(implementation = ResponseMainInfoDto.class))),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음",
                    content = @Content(schema = @Schema(example = "{\"message\": \"채팅방을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버에러",
                    content = @Content(schema = @Schema(example = "{\"message\": \"서버에러\"}")))
    })
    @PostMapping("/{chatRoomId}/update")
    public ResponseEntity<?> updateChatRoomInfo(@RequestHeader("X-User-Id") String userId, @PathVariable Long chatRoomId, @RequestBody RequestUpdateChatRoomDto requestUpdateChatRoomDto) {

        try {
            chatRoomService.updateChatRoomInfo(chatRoomId, requestUpdateChatRoomDto);
            return ResponseEntity.ok(Map.of("message", "업데이트 성공"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "채팅방을 찾을 수 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버에러"));
        }
    }

    @Operation(summary = "채팅방 나가기", description = "채팅방 나가기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "나가기 성공",
                    content = @Content(schema = @Schema(implementation = ResponseMainInfoDto.class))),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음",
                    content = @Content(schema = @Schema(example = "{\"message\": \"채팅방을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버에러",
                    content = @Content(schema = @Schema(example = "{\"message\": \"서버에러\"}")))
    })
    @PostMapping("/{chatRoomId}/exit")
    public ResponseEntity<?> exitChatRoom(@RequestHeader("X-User-Id") String userId, @PathVariable Long chatRoomId) {

        try {
            chatRoomService.exitChatRoom(Long.parseLong(userId), chatRoomId);
            return ResponseEntity.ok(Map.of("message", "나가기 성공"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "채팅방을 찾을 수 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버에러"));
        }
    }

    @Operation(summary = "채팅방 참여", description = "초대받은 채팅방 참여")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "참여 성공",
                    content = @Content(schema = @Schema(implementation = ResponseMainInfoDto.class))),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음",
                    content = @Content(schema = @Schema(example = "{\"message\": \"채팅방을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버에러",
                    content = @Content(schema = @Schema(example = "{\"message\": \"서버에러\"}")))
    })
    @PostMapping("/{chatRoomId}/join")
    public ResponseEntity<?> joinChatRoom(@RequestHeader("X-User-Id") String userId, @PathVariable Long chatRoomId) {

        try {
            boolean joined = chatRoomService.joinChatRoom(Long.parseLong(userId), chatRoomId);
            if (joined) {
                return ResponseEntity.ok(Map.of(
                        "status", "ok",
                        "message", "참여 성공",
                        "chatRoomId", chatRoomId
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "status", "fail",
                        "message", "채팅방 최대 인원 초과",
                        "chatRoomId", null
                ));
            }

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "채팅방을 찾을 수 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버에러"));
        }
    }

    // Todo 채팅방 초대 만들기

    @Operation(summary = "채팅 메세지 목록 조회", description = "채팅 메세지 목록")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MessageDto.class))))
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<?> getMessages(@RequestHeader("X-User-Id") String userId, @PathVariable Long chatRoomId, @RequestParam(required = false) String lastMessageId) {

        List<MessageDto> messageList = chatRoomService.getMessages(Long.parseLong(userId), chatRoomId, lastMessageId);
        return ResponseEntity.ok(Map.of("messageList", messageList));
    }

    @Operation(summary = "채팅 메세지 목록 새로고침", description = "채팅 메세지 목록")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MessageDto.class))))
    @GetMapping("/{chatRoomId}/refresh")
    public ResponseEntity<?> getRefreshMessages(@RequestHeader("X-User-Id") String userId, @PathVariable Long chatRoomId, @RequestParam(required = false) String lastMessageId) {

        List<MessageDto> messageList = chatRoomService.getRefreshMessages(Long.parseLong(userId), chatRoomId, lastMessageId);
        return ResponseEntity.ok(Map.of("messageList", messageList));
    }
}
