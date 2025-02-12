package com.badboys.unbound_chat.api;

import com.badboys.unbound_chat.api.model.ChatMessage;
import com.badboys.unbound_chat.api.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ChatKafkaListener {

    private final ChatService chatService;

    @Autowired
    public ChatKafkaListener(ChatService chatService) {
        this.chatService = chatService;
    }

    @KafkaListener(
            topics = "chat-message-topic",
            groupId = "${spring.kafka.consumer.chat.group-id}",
            containerFactory = "chatKafkaListenerContainerFactory"  // ✅ 새 Factory 적용!
    )
    public void chatListen(ChatMessage chatMessage) {
        log.info("Kafka에서 채팅 메시지 수신: {}", chatMessage);
        chatService.sendMessage(chatMessage);
    }
}
