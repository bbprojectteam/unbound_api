package com.badboys.unbound_service.api;

import com.badboys.unbound_service.api.repository.CommentRepository;
import com.badboys.unbound_service.api.repository.MatchInfoRepository;
import com.badboys.unbound_service.api.repository.UserRepository;
import com.badboys.unbound_service.api.service.ChatRoomService;
import com.badboys.unbound_service.api.service.RegionService;
import com.badboys.unbound_service.api.service.UserService;
import com.badboys.unbound_service.model.ChatRoomThumbnail;
import com.badboys.unbound_service.model.ResponseMainInfoDto;
import com.badboys.unbound_service.model.UserInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
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
            content = @Content(schema = @Schema(implementation = ResponseMainInfoDto.class)))
    @GetMapping("/list")
    public ResponseEntity<?> getChatRoomList(@RequestHeader("X-User-Id") String userId) {

        List<ChatRoomThumbnail> chatRoomList = chatRoomService.getChatRoomList(Long.parseLong(userId));
        return ResponseEntity.ok(Map.of("chatRoomList", chatRoomList));
    }

    @Operation(summary = "채팅방 목록 조회", description = "채팅방 목록")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ResponseMainInfoDto.class)))
    @GetMapping("/info/{chatRoomId}")
    public ResponseEntity<?> getChatRoomInfo(@RequestHeader("X-User-Id") String userId, @PathVariable Long chatRoomId) {

//        UserInfoDto userInfoDto = chatRoomService.getChatRoomInfo(chatRoomId);
//        if (userInfoDto.getRegionNm() == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "지역 정보 없음"));
//        }
//
//        ResponseMainInfoDto responseMainInfoDto = matchService.getMainMatchHistoryList(userInfoDto);
//        responseMainInfoDto.setUserInfo(userInfoDto);
//        return ResponseEntity.ok(responseMainInfoDto);
        return null;
    }
}
