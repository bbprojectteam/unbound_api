package com.badboys.unbound_chat.api.model;

import lombok.Data;

@Data
public class ReadMessage {

    private Long userId;

    private Long chatRoomId;

    private Long chatMessageId;
}
