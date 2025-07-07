package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "채팅 메시지 DTO")
public class MessageDto {

    @Schema(description = "메세지 ID", example = "123")
    private Long chatMessageId;

    @Schema(description = "보낸 유저 ID", example = "123")
    private Long senderId;

    @Schema(description = "유저명", example = "wukim")
    private String username;

    @Schema(description = "유저 프로필 사진", example = "")
    private String profileImage;

    @Schema(description = "메시지 내용", example = "안녕하세요")
    private String message;

    @Schema(description = "생성 시간", example = "2025-02-18T15:30:00Z")
    private String createdAt;

    @Schema(description = "안 읽은 사람 수", example = "3")
    private int unreadMemberCnt;
}
