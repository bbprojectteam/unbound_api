package com.badboys.unbound_chat.api.model;

import lombok.Data;

@Data
public class ChatMessage {

    private Long chatRoomId;

    private Long senderId;

    private Long chatMessageId;

    private String message;

    private String imageUrl;
}
