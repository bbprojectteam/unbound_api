package com.badboys.unbound_match.service;

import com.badboys.unbound_match.model.RequestMatchDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final long EXPIRATION_TIME = 86400000L;

    @Autowired
    public MatchService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper, KafkaTemplate<String, Object> kafkaTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void matchRequest(RequestMatchDto request) {
        log.info("userId : " + request.getUserId());
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            long expireTime = System.currentTimeMillis() + EXPIRATION_TIME;
            String userId = request.getUserId();

            connection.zAdd("match_user_mmr".getBytes(), (double) request.getMmr(), userId.getBytes());
            connection.zAdd("match_queue".getBytes(), (double) expireTime, userId.getBytes());
            try {
                connection.hSet("match_user_regions".getBytes(), userId.getBytes(), objectMapper.writeValueAsBytes(request.getRegionIdList()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize region data", e);
            }
            return null;
        });

        matchQueue();
    }

    public void matchQueue() {

        long currentTime = System.currentTimeMillis();

        Set<ZSetOperations.TypedTuple<Object>> queuedUsers = redisTemplate.opsForZSet().rangeByScoreWithScores("match_queue", currentTime, Double.POSITIVE_INFINITY);
        if (queuedUsers.isEmpty()) return;

        Map<String, Double> userExpireMap = queuedUsers.stream()
                .collect(Collectors.toMap(
                        entry -> entry.getValue().toString(),
                        entry -> entry.getScore()
                ));

        Map<String, List<String>> userRegionMap = redisTemplate.opsForHash()
                .entries("match_user_regions")
                .entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> (List<String>) entry.getValue()
                ));

        for (ZSetOperations.TypedTuple<Object> entry : queuedUsers) {
            String userId = entry.getValue().toString();
            Double userMmr = redisTemplate.opsForZSet().score("match_user_mmr", entry.getValue());
            if (userMmr == null) continue;
            matchProcess(userId, userMmr, userExpireMap.get(userId), userRegionMap, userExpireMap);
        }
    }

    private void matchProcess(String userId, double mmr, double userExpireTime, Map<String, List<String>> userRegionMap, Map<String, Double> userExpireMap) {
        double elapsedSeconds = (System.currentTimeMillis() - (userExpireTime - EXPIRATION_TIME)) / 1000;
        double mmrMultiplier = elapsedSeconds > 900 ? 1.2 : elapsedSeconds > 600 ? 1.15 : elapsedSeconds > 300 ? 1.10 : 1.05;
        double minMmr = mmr * (2 - mmrMultiplier);
        double maxMmr = mmr * mmrMultiplier;

        List<String> potentialMatches = findMatchingUsers(userId, minMmr, maxMmr, userRegionMap, userExpireMap);
        if (potentialMatches.size() == 6) {
            matchSuccess(potentialMatches);
        }
    }

    public List<String> findMatchingUsers(String currentUserId, double minMmr, double maxMmr, Map<String, List<String>> userRegionMap, Map<String, Double> userExpireMap) {
        Set<Object> matchedUsers = redisTemplate.opsForZSet().rangeByScore("match_user_mmr", minMmr, maxMmr);
        matchedUsers.remove(Integer.parseInt(currentUserId));
        if (matchedUsers == null || matchedUsers.isEmpty()) return Collections.emptyList();

        List<String> currentUserRegions = userRegionMap.getOrDefault(currentUserId, Collections.emptyList());;

        List<String> validUsers = new ArrayList<>();
        validUsers.add(currentUserId);
        long currentTime = System.currentTimeMillis();
        for (Object userId : matchedUsers) {
            Double expireTime = userExpireMap.get(userId.toString());
            if (expireTime == null || expireTime < currentTime || currentUserId.equals(userId.toString())) continue;

            List<String> targetUserRegions = userRegionMap.getOrDefault(userId.toString(), Collections.emptyList());

            if (!Collections.disjoint(currentUserRegions, targetUserRegions)) {
                validUsers.add(userId.toString());
            }

            if (validUsers.size() == 6) break;
        }
        return validUsers;
    }

    public void matchSuccess(List<String> matchedUsers) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String userId : matchedUsers) {
                connection.zRem("match_user_mmr".getBytes(), userId.getBytes());
                connection.zRem("match_queue".getBytes(), userId.getBytes());
                connection.hDel("match_user_regions".getBytes(), userId.getBytes());
            }
            return null;
        });
        kafkaTemplate.send("match-success-topic", matchedUsers);
    }
}






