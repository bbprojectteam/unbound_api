package com.badboys.unbound_chat.api;

import com.badboys.unbound_chat.api.model.RequestCreateChatRoomDto;
import com.badboys.unbound_chat.api.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ChatRoomKafkaListener {

    private final ChatService chatService;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_KEY = "CHAT_LOCK";

    @Autowired
    public ChatRoomKafkaListener(ChatService chatService, RedisTemplate<String, Object> redisTemplate) {
        this.chatService = chatService;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "match-success-topic", groupId = "chat-consumer-group")
    public void consumeCreateChatRoomRequest(RequestCreateChatRoomDto request) {

        // 분산락 적용
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "LOCK", 5, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return; // 다른 프로세스가 매칭 중이면 중복 실행 방지
        }
        try {
            chatService.createChatRoom(request);
        } finally {
            redisTemplate.delete(LOCK_KEY);
        }

    }
}
