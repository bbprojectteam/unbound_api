package com.badboys.unbound_service.api.service;

import com.badboys.unbound_service.api.repository.MatchHistoryRepository;
import com.badboys.unbound_service.entity.UserEntity;
import com.badboys.unbound_service.model.MatchHistoryDto;
import com.badboys.unbound_service.model.RequestMatchDto;
import com.badboys.unbound_service.model.ResponseMainInfoDto;
import com.badboys.unbound_service.model.UserInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class MatchService {

    private final UserService userService;
    private final RegionService regionService;
    private final MatchHistoryRepository matchHistoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public MatchService(UserService userService, RegionService regionService, MatchHistoryRepository matchHistoryRepository, KafkaTemplate<String, Object> kafkaTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.regionService = regionService;
        this.matchHistoryRepository = matchHistoryRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
    }

    public boolean isMatchable(String userId) {
        boolean result = false;
        try {
            long currentTime = System.currentTimeMillis();
            Double expireTime = redisTemplate.opsForZSet().score("match_queue", Integer.parseInt(userId));
            if (expireTime == null || expireTime < currentTime) {
                result = true;
            }
        } catch (Exception e) {
            log.error("매칭 확인 에러" + e.getMessage());
        }
        return result;
    }

    public boolean startMatch(Long userId, Long limitRegionId) {
        try {
            UserEntity userEntity = userService.getUserEntity(userId);
            if (userEntity == null) {
                throw new IllegalArgumentException("유저 정보를 찾을 수 없습니다.");
            }

            List<Long> regionIdList = regionService.getAllChildrenId(limitRegionId);
            int mmr = userEntity.getMmr();

            RequestMatchDto requestMatchDto = new RequestMatchDto(userId.toString(), mmr, regionIdList);

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send("match-request-topic", requestMatchDto);

            future.thenAccept(result -> {
                log.info("Kafka 메시지 전송 성공: " + requestMatchDto);
            }).exceptionally(ex -> {
                log.error("Kafka 메시지 전송 실패: " + ex.getMessage());
                throw new RuntimeException("Kafka 메시지 전송 실패", ex);
            });

            return true; // 성공적으로 전송된 경우

        } catch (Exception e) {
            log.error("매칭 시작 실패: " + e.getMessage());
            return false;
        }
    }

    public void cancelMatch(String userId) {
        try {
            if (isMatchable(userId)) return;
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                connection.zRem("match_user_mmr".getBytes(), userId.getBytes());
                connection.zRem("match_queue".getBytes(), userId.getBytes());
                connection.hDel("match_user_regions".getBytes(), userId.getBytes());
                return null;
            });
        } catch (Exception e) {
            log.error("매칭 취소 중 에러" + e.getMessage());
        }
    }

    public ResponseMainInfoDto getMainMatchHistoryList(UserInfoDto userInfoDto) {

        List<MatchHistoryDto> userMatchHistoryList = matchHistoryRepository.findByUserId(userInfoDto.getUserId());
        List<MatchHistoryDto> regionMatchHistoryList = matchHistoryRepository.findByRegionId(userInfoDto.getUserId());
        ResponseMainInfoDto responseMainInfoDto = new ResponseMainInfoDto();
        responseMainInfoDto.setUserMatchHistoryList(userMatchHistoryList);
        responseMainInfoDto.setRegionMatchHistoryList(regionMatchHistoryList);
        return responseMainInfoDto;
    }
}
