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
import org.springframework.web.multipart.MultipartFile;

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
            @ApiResponse(responseCode = "200", description = "업데이트 성공"),
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
            @ApiResponse(responseCode = "200", description = "나가기 성공"),
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
            @ApiResponse(responseCode = "200", description = "참여 성공"),
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

    @Operation(summary = "채팅 메세지 목록 조회", description = "채팅 메세지 목록")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MessageDto.class))))
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ResponseMessageListDto> getMessages(@RequestHeader("X-User-Id") String userId, @PathVariable Long chatRoomId, @RequestParam(required = false) Long lastMessageId) {

        ResponseMessageListDto responseMessageListDto = chatRoomService.getMessages(Long.parseLong(userId), chatRoomId, lastMessageId);
        return ResponseEntity.ok(responseMessageListDto);
    }

    @Operation(summary = "채팅 이미지 업로드", description = "채팅 메세지 목록")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @PostMapping("/{chatRoomId}/uploadImage")
    public ResponseEntity<?> uploadChatImage(@RequestHeader("X-User-Id") String userId, @PathVariable Long chatRoomId, @RequestPart(value = "profileImageFile") MultipartFile imageFile) {

        String imageUrl = chatRoomService.uploadImage(imageFile);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @Operation(summary = "방장 역할 변경", description = "방장 넘기기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "역할 변경 성공"),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음",
                    content = @Content(schema = @Schema(example = "{\"message\": \"채팅방을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "500", description = "서버에러",
                    content = @Content(schema = @Schema(example = "{\"message\": \"서버에러\"}")))
    })
    @PostMapping("/{chatRoomId}/changeOwner/{targetUserId}")
    public ResponseEntity<?> changeOwner(@RequestHeader("X-User-Id") String userId, @PathVariable Long chatRoomId, @PathVariable Long targetUserId) {

        try {
            chatRoomService.changeOwner(Long.parseLong(userId), chatRoomId, targetUserId);
            return ResponseEntity.ok(Map.of("message", "역할 변경 성공"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "채팅방을 찾을 수 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버에러"));
        }
    }

    @Operation(summary = "초대할 유저 목록 조회", description = "지역 기반 유저목록")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserSimpleDto.class))))
    @GetMapping("/{chatRoomId}/invitation/list")
    public ResponseEntity<?> getInvitationList(@RequestHeader("X-User-Id") String userId, @PathVariable Long chatRoomId) {

        List<UserSimpleDto> invitableList = chatRoomService.getInvitationList(chatRoomId);
        return ResponseEntity.ok(Map.of("invitableList", invitableList));
    }

    @Operation(summary = "유저 초대", description = "유저 초대")
    @ApiResponse(responseCode = "200", description = "초대 성공")
    @PostMapping("/{chatRoomId}/invitation/{targetUserId}")
    public ResponseEntity<?> inviteUser(@RequestHeader("X-User-Id") String userId, @PathVariable Long chatRoomId, @PathVariable Long targetUserId) {

        try {
            chatRoomService.inviteUser(chatRoomId, targetUserId);
            return ResponseEntity.ok(Map.of("message", "초대 성공"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "카프카 오류"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버에러"));
        }
    }
}
