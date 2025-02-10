package com.badboys.unbound_match.service;

import com.badboys.unbound_match.model.RegionData;
import com.badboys.unbound_match.model.RequestMatchDto;
import com.badboys.unbound_match.model.ResponseMatchSuccessDto;
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
            Long userId = request.getUserId();

            connection.zAdd("match_user_mmr".getBytes(), (double) request.getMmr(), userId.toString().getBytes());
            connection.zAdd("match_queue".getBytes(), (double) expireTime, userId.toString().getBytes());
            try {
                RegionData regionData = new RegionData(request.getRegionRange(), request.getRegionId());
                connection.hSet("match_user_regions".getBytes(), userId.toString().getBytes(), objectMapper.writeValueAsBytes(regionData));
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
        if (Objects.requireNonNull(queuedUsers).isEmpty()) return;

        Map<Long, Double> userExpireMap = queuedUsers.stream()
                .collect(Collectors.toMap(
                        entry -> ((Integer) entry.getValue()).longValue(),
                        entry -> entry.getScore()
                ));


        Map<Object, Object> redisRegionData = redisTemplate.opsForHash().entries("match_user_regions");

        Map<Long, List<Long>> userRegionRangeMap = new HashMap<>();
        Map<Long, Long> userRegionIdMap = new HashMap<>();

        for (Map.Entry<Object, Object> entry : redisRegionData.entrySet()) {
            try {
                Long userId = Long.parseLong(entry.getKey().toString());  // ✅ userId 가져오기
                RegionData regionData = objectMapper.convertValue(entry.getValue(), RegionData.class); // ✅ JSON 파싱

                userRegionRangeMap.put(userId, regionData.getRegionRange());
                userRegionIdMap.put(userId, regionData.getRegionId());
            } catch (Exception e) {
                throw new RuntimeException("JSON 파싱 오류: " + entry.getValue(), e);
            }
        }

        for (ZSetOperations.TypedTuple<Object> entry : queuedUsers) {
            Long userId = Long.parseLong(entry.getValue().toString());
            Double userMmr = redisTemplate.opsForZSet().score("match_user_mmr", entry.getValue());
            if (userMmr == null) continue;
            matchProcess(userId, userMmr, userExpireMap.get(userId), userRegionRangeMap, userExpireMap, userRegionIdMap);
        }
    }

    private void matchProcess(Long userId, double mmr, double userExpireTime, Map<Long, List<Long>> userRegionRangeMap, Map<Long, Double> userExpireMap, Map<Long, Long> userRegionIdMap) {
        double elapsedSeconds = (System.currentTimeMillis() - (userExpireTime - EXPIRATION_TIME)) / 1000;
        double mmrMultiplier = elapsedSeconds > 900 ? 1.2 : elapsedSeconds > 600 ? 1.15 : elapsedSeconds > 300 ? 1.10 : 1.05;
        double minMmr = mmr * (2 - mmrMultiplier);
        double maxMmr = mmr * mmrMultiplier;

        Set<Long> potentialMatches = findMatchingUsers(userId, minMmr, maxMmr, userRegionRangeMap, userExpireMap);
        if (potentialMatches.size() == 6) {
            matchSuccess(potentialMatches, userRegionIdMap);
        }
    }

    public Set<Long> findMatchingUsers(Long currentUserId, double minMmr, double maxMmr, Map<Long, List<Long>> userRegionRangeMap, Map<Long, Double> userExpireMap) {
        Set<Long> matchedUsers = redisTemplate.opsForZSet()
                .rangeByScore("match_user_mmr", minMmr, maxMmr)
                .stream()
                .map(obj -> Long.parseLong(obj.toString()))  // ✅ `Object` → `Long` 변환
                .collect(Collectors.toSet());
        matchedUsers.remove(currentUserId);
        if (matchedUsers == null || matchedUsers.isEmpty()) return Collections.emptySet();

        List<Long> currentUserRegions = userRegionRangeMap.getOrDefault(currentUserId, Collections.emptyList());;

        Set<Long> validUsers = new HashSet<>();
        validUsers.add(currentUserId);
        long currentTime = System.currentTimeMillis();
        for (Object userId : matchedUsers) {
            Long tempUserId = Long.parseLong(userId.toString());
            Double expireTime = userExpireMap.get(tempUserId);
            if (expireTime == null || expireTime < currentTime || currentUserId.equals(tempUserId)) continue;

            List<Long> targetUserRegions = userRegionRangeMap.getOrDefault(tempUserId, Collections.emptyList());

            if (!Collections.disjoint(currentUserRegions, targetUserRegions)) {
                validUsers.add(tempUserId);
            }

            if (validUsers.size() == 6) break;
        }
        return validUsers;
    }

    public void matchSuccess(Set<Long> matchSuccessUsers, Map<Long, Long> userRegionIdMap) {

        Set<Long> regionIdSet = new HashSet<>();

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long userId : matchSuccessUsers) {
                connection.zRem("match_user_mmr".getBytes(), userId.toString().getBytes());
                connection.zRem("match_queue".getBytes(), userId.toString().getBytes());
                connection.hDel("match_user_regions".getBytes(), userId.toString().getBytes());
                regionIdSet.add(userRegionIdMap.get(userId));
            }
            return null;
        });

        kafkaTemplate.send("match-success-topic", new ResponseMatchSuccessDto(matchSuccessUsers, regionIdSet));
    }
}






