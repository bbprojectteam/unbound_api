package com.badboys.unbound_chat.api;

import com.badboys.unbound_chat.api.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ChatKafkaListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatKafkaListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(
            topics = "chat-topic",
            groupId = "${spring.kafka.consumer.chat.group-id}",
            containerFactory = "chatKafkaListenerContainerFactory"  // ✅ 새 Factory 적용!
    )
    public void listen(ChatMessage chatMessage) {
        log.info("Kafka에서 채팅 메시지 수신: {}", chatMessage);

        // WebSocket을 통해 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat/" + chatMessage.getChatRoomId(), chatMessage, messageHeaders(chatMessage.getSenderId()));
    }

    private Map<String, Object> messageHeaders(String senderId) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("exclude-sender", senderId);  // 메세지 보낸 유저를 WebSocket에서 제외하기 위한 커스텀 헤더 추가
        return headers;
    }
}
