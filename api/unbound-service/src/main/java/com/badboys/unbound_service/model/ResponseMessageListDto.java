package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "채팅방 메세지 목록 DTO")
public class ResponseMessageListDto {

    @Schema(description = "최근 메세지 목록", example = "")
    private List<MessageDto> messageList;

    @Schema(description = "총 메세지 카운트", example = "100")
    private int messageCnt;
}
