package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "채팅방 정보 DTO")
public class ResponseChatRoomInfoDto {

    @Schema(description = "채팅방 정보")
    ChatRoomInfo chatRoomInfo;

    @Schema(description = "채팅방 참가 유저 목록")
    private List<ChatMemberDto> memberList;
}
