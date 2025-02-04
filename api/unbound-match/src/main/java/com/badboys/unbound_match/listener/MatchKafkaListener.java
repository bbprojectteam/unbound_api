package com.badboys.unbound_match.listener;

import com.badboys.unbound_match.model.RequestMatchDto;
import com.badboys.unbound_match.service.MatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MatchKafkaListener {

    private final MatchService matchService;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_KEY = "MATCH_LOCK";

    @Autowired
    public MatchKafkaListener(MatchService matchService, RedisTemplate<String, Object> redisTemplate) {
        this.matchService = matchService;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "match-request-topic", groupId = "match-consumer-group")
    public void consumeMatchRequest(RequestMatchDto request) {

        // 분산락 적용
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "LOCK", 5, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return; // 다른 프로세스가 매칭 중이면 중복 실행 방지
        }
        try {
            matchService.matchRequest(request);
        } finally {
            redisTemplate.delete(LOCK_KEY);
        }

    }
}
