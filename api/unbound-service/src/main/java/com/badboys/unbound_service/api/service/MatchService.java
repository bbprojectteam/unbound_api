package com.badboys.unbound_service.api.service;

import com.badboys.unbound_service.entity.UserEntity;
import com.badboys.unbound_service.model.RequestMatchDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class MatchService {

    private final UserService userService;

    private final ModelMapper modelMapper;

    private final RegionService regionService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public MatchService(UserService userService, ModelMapper modelMapper, RegionService regionService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.regionService = regionService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public boolean getMatchStart(Long userId, Long limitRegionId) {
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
                System.out.println("Kafka 메시지 전송 성공: " + requestMatchDto);
            }).exceptionally(ex -> {
                System.err.println("Kafka 메시지 전송 실패: " + ex.getMessage());
                throw new RuntimeException("Kafka 메시지 전송 실패", ex);
            });

            return true; // 성공적으로 전송된 경우

        } catch (Exception e) {
            System.err.println("매칭 시작 실패: " + e.getMessage());
            return false;
        }
    }


}
