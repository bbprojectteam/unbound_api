package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.service.ChatRoomService;
import com.badboys.unbound_service.model.ChatRoomThumbnailDto;
import com.badboys.unbound_service.model.MessageDto;
import com.badboys.unbound_service.model.ResponseChatRoomInfoDto;
import com.badboys.unbound_service.model.ResponseMainInfoDto;
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

    @Operation(summary = "채팅방 목록 조회", description = "채팅방 목록")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChatRoomThumbnailDto.class))))
    @GetMapping("/list")
    public ResponseEntity<?> getChatRoomList(@RequestHeader("X-User-Id") String userId) {

        List<ChatRoomThumbnailDto> chatRoomList = chatRoomService.getChatRoomList(Long.parseLong(userId));
        return ResponseEntity.ok(Map.of("chatRoomList", chatRoomList));
    }

    @Operation(summary = "채팅방 정보 조회", description = "채팅방 목록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ResponseMainInfoDto.class))),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음",
                    content = @Content(schema = @Schema(example = "{\"message\": \"채팅방을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버에러",
                    content = @Content(schema = @Schema(example = "{\"message\": \"서버에러\"}")))
    })
    @GetMapping("/{chatRoomId}/info")
    public ResponseEntity<?> getChatRoomInfo(@RequestHeader("X-User-Id") String userId, @PathVariable Long chatRoomId) {

        try {
            ResponseChatRoomInfoDto responseChatRoomInfoDto = chatRoomService.getChatRoomInfo(chatRoomId);
            return ResponseEntity.ok(responseChatRoomInfoDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "채팅방을 찾을 수 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버에러"));
        }
    }

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
