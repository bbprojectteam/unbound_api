package com.badboys.unbound_chat.api;

import com.badboys.unbound_chat.api.model.ChatMessage;
import com.badboys.unbound_chat.api.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")  // 클라이언트가 "/app/chat.sendMessage"로 보낸 메시지를 받음
    public void sendMessage(@Payload ChatMessage chatMessage) {

        log.info("클라이언트에서 채팅 메시지 수신: {}", chatMessage);
        chatService.publishMessage(chatMessage);
    }
}
