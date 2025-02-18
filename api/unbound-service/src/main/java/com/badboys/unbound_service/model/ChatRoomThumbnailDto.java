package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "채팅방 썸네일 DTO")
public class ChatRoomThumbnailDto {

    @Schema(description = "채팅방 아이디", example = "구독용 아이디")
    private Long chatRoomId;

    @Schema(description = "채팅방 이름", example = "농구 매칭 채팅방")
    private String name;

    @Schema(description = "안 읽은 메시지 개수", example = "5")
    private int unreadCnt;

    @Schema(description = "마지막 메시지", example = "안녕하세요")
    private String lastMessage;

    @Schema(description = "마지막 메시지 생성 시간", example = "2025-02-18T15:30:00Z")
    private String lastMessageCreatedAt;
}

