package com.badboys.unbound_chat.api.service;

import com.badboys.unbound_chat.api.entity.ChatMemberEntity;
import com.badboys.unbound_chat.api.repository.ChatMemberRepository;
import org.springframework.data.mongodb.core.MongoOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RedisService {

    private final RedisTemplate redisTemplate;

    private final ChatMemberRepository chatMemberRepository;

    public RedisService(RedisTemplate redisTemplate, ChatMemberRepository chatMemberRepository) {
        this.redisTemplate = redisTemplate;
        this.chatMemberRepository = chatMemberRepository;
    }

    public void saveLastReadMessage(Long roomId, Long userId, Long messageId) {
        String key = "chat_read:room:" + roomId;
        redisTemplate.opsForHash().put("chat_read:room:" + roomId, userId.toString(), messageId.toString());
    }

    public Map<Long, Long> getAllUserReadStatus(Long roomId) {
        String key = "chat_read:room:" + roomId;
        Map<Object, Object> raw = redisTemplate.opsForHash().entries(key);

        // Redis에 데이터 없으면 → RDB에서 조회해서 Redis에 채워넣기
        if (raw == null || raw.isEmpty()) {
            log.info("Redis에 읽음 상태 없음 RDB 조회 (chatRoomId={})", roomId);

            List<ChatMemberEntity> memberList = chatMemberRepository.findByChatRoomId(roomId);
            for (ChatMemberEntity member : memberList) {
                Long userId = member.getUser().getId();
                String lastReadMessageId = member.getLastReadMessageId();
                if (lastReadMessageId != null) {
                    redisTemplate.opsForHash().put(key, userId.toString(), lastReadMessageId);
                }
            }

            // 다시 Redis에서 가져오기
            raw = redisTemplate.opsForHash().entries(key);
        }

        return raw.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> Long.valueOf(e.getKey().toString()),
                        e -> Long.valueOf(e.getValue().toString())
                ));
    }
}
