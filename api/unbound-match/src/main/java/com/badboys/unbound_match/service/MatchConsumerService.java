package com.badboys.unbound_match.service;

import com.badboys.unbound_match.model.RequestMatchDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MatchConsumerService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final long EXPIRATION_TIME = 86400000L;
    private static final String LOCK_KEY = "MATCH_LOCK";

    @Autowired
    public MatchConsumerService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper, KafkaTemplate<String, Object> kafkaTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "match-request-topic", groupId = "match-consumer-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeMatchRequest(RequestMatchDto request) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            long expireTime = System.currentTimeMillis() + EXPIRATION_TIME;
            String userId = request.getUserId();

            connection.zAdd("match_queue".getBytes(), (double) request.getMmr(), userId.getBytes());
            connection.zAdd("match_expire_queue".getBytes(), (double) expireTime, userId.getBytes());
            try {
                connection.hSet("user_regions".getBytes(), userId.getBytes(), objectMapper.writeValueAsBytes(request.getRegionIdList()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize region data", e);
            }
            return null;
        });

        // 분산락 적용
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "LOCK", 5, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return; // 다른 프로세스가 매칭 중이면 중복 실행 방지
        }
        try {
            processMatchQueue();
        } finally {
            redisTemplate.delete(LOCK_KEY);
        }
    }

    public void processMatchQueue() {
        Set<ZSetOperations.TypedTuple<Object>> queuedUsers = redisTemplate.opsForZSet().rangeWithScores("match_queue", 0, -1);
        if (queuedUsers.isEmpty()) return;

        long currentTime = System.currentTimeMillis();

        Map<String, List<String>> userRegionMap = redisTemplate.opsForHash().entries("user_regions").entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry.getKey(),
                        entry -> {
                            try {
                                return objectMapper.readValue((String) entry.getValue(), new TypeReference<List<String>>() {});
                            } catch (JsonProcessingException e) {
                                return Collections.emptyList();
                            }
                        }
                ));
        Map<Object, Double> userExpireMap = new HashMap<>();
        Set<ZSetOperations.TypedTuple<Object>> expireEntries = redisTemplate.opsForZSet().rangeWithScores("match_expire_queue", currentTime, Long.MAX_VALUE);
        if (expireEntries != null) {
            for (ZSetOperations.TypedTuple<Object> entry : expireEntries) {
                userExpireMap.put(entry.getValue(), entry.getScore());
            }
        }

        for (ZSetOperations.TypedTuple<Object> entry : queuedUsers) {
            String userId = (String) entry.getValue();
            Double userMmr = entry.getScore();
            Double userExpireTime = userExpireMap.get(userId);
            if (userExpireTime == null || userExpireTime < currentTime) continue;
            matchProcess(userId, userMmr, userExpireTime, userRegionMap, userExpireMap);
        }
    }

    private void matchProcess(String userId, double mmr, double userExpireTime, Map<String, List<String>> userRegionMap, Map<Object, Double> userExpireMap) {
        double elapsedSeconds = (EXPIRATION_TIME - (System.currentTimeMillis() - userExpireTime)) / 1000;
        double mmrMultiplier = elapsedSeconds > 900 ? 1.30 : elapsedSeconds > 600 ? 1.20 : elapsedSeconds > 300 ? 1.10 : 1.05;
        double minMmr = mmr * (2 - mmrMultiplier);
        double maxMmr = mmr * mmrMultiplier;

        List<String> potentialMatches = findMatchingUsers(userId, minMmr, maxMmr, userRegionMap, userExpireMap);
        if (potentialMatches.size() >= 6) {
            matchSuccess(potentialMatches.subList(0, 6));
        }
    }

    public List<String> findMatchingUsers(String currentUserId, double minMmr, double maxMmr, Map<String, List<String>> userRegionMap, Map<Object, Double> userExpireMap) {
        Set<Object> matchedUsers = redisTemplate.opsForZSet().rangeByScore("match_queue", minMmr, maxMmr);
        if (matchedUsers == null || matchedUsers.isEmpty()) return Collections.emptyList();

        List<String> currentUserRegions = userRegionMap.getOrDefault(currentUserId, Collections.emptyList());;

        List<String> validUsers = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (Object userId : matchedUsers) {
            Double expireTime = userExpireMap.get(userId);
            if (expireTime == null || expireTime < currentTime) continue;

            List<String> targetUserRegions = userRegionMap.getOrDefault((String) userId, Collections.emptyList());;

            if (!Collections.disjoint(currentUserRegions, targetUserRegions)) {
                validUsers.add((String) userId);
            }
        }
        return validUsers;
    }

    public void matchSuccess(List<String> matchedUsers) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String userId : matchedUsers) {
                connection.zRem("match_queue".getBytes(), userId.getBytes());
                connection.zRem("match_expire_queue".getBytes(), userId.getBytes());
                connection.hDel("user_regions".getBytes(), userId.getBytes());
            }
            return null;
        });

        kafkaTemplate.send("match-success-topic", matchedUsers);
    }
}






