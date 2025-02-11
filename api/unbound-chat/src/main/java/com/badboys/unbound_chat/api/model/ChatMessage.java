package com.badboys.unbound_chat.api.model;

import lombok.Data;

@Data
public class ChatMessage {

    private String chatRoomId;

    private String senderId;

    private String message;
}
