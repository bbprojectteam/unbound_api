package com.badboys.unbound_service.api.service;

import com.badboys.unbound_service.api.repository.CommentRepository;
import com.badboys.unbound_service.api.repository.MatchInfoRepository;
import com.badboys.unbound_service.api.repository.UserRepository;
import com.badboys.unbound_service.entity.CommentEntity;
import com.badboys.unbound_service.entity.MatchInfoEntity;
import com.badboys.unbound_service.entity.TeamEntity;
import com.badboys.unbound_service.entity.UserEntity;
import com.badboys.unbound_service.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchService {

    private final UserService userService;
    private final RegionService regionService;
    private final UserRepository userRepository;
    private final MatchInfoRepository matchInfoRepository;
    private final CommentRepository commentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public MatchService(UserService userService, RegionService regionService, UserRepository userRepository, MatchInfoRepository matchInfoRepository, CommentRepository commentRepository, KafkaTemplate<String, Object> kafkaTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.regionService = regionService;
        this.userRepository = userRepository;
        this.matchInfoRepository = matchInfoRepository;
        this.commentRepository = commentRepository;
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

        Page<MatchInfoEntity> userMatchInfoEntityList = matchInfoRepository.findByUserId(userInfoDto.getUserId(), PageRequest.of(0, 5));
        List<MatchHistoryDto> userMatchHistoryList = userMatchInfoEntityList.stream()
                .map(this::convertToMatchHistoryDto)
                .collect(Collectors.toList());

        Page<MatchInfoEntity> regionMatchInfoEntityList = matchInfoRepository.findByRegionId(userInfoDto.getUserId(), PageRequest.of(0, 5));
        List<MatchHistoryDto> regionMatchHistoryList = regionMatchInfoEntityList.stream()
                .map(this::convertToMatchHistoryDto)
                .collect(Collectors.toList());

        ResponseMainInfoDto responseMainInfoDto = new ResponseMainInfoDto();
        responseMainInfoDto.setUserMatchHistoryList(userMatchHistoryList);
        responseMainInfoDto.setRegionMatchHistoryList(regionMatchHistoryList);

        return responseMainInfoDto;
    }

    private MatchHistoryDto convertToMatchHistoryDto(MatchInfoEntity matchHistory) {
        List<TeamInfoDto> teamList = convertToTeamInfoDto(matchHistory.getTeamList());

        return new MatchHistoryDto(
                matchHistory.getId(),
                matchHistory.getStartAt(),
                matchHistory.getEndAt(),
                matchHistory.getRegion().getId(),
                teamList
        );
    }

    private List<TeamInfoDto> convertToTeamInfoDto(List<TeamEntity> teamEntities) {
        return teamEntities.stream()
                .map(team -> {
                    List<UserSimpleDto> userList = convertToUserSimpleDto(team.getUserList());
                    return new TeamInfoDto(team.getId(), team.getResult(), userList);
                })
                .collect(Collectors.toList());
    }

    private List<UserSimpleDto> convertToUserSimpleDto(List<UserEntity> userEntities) {
        return userEntities.stream()
                .map(user -> new UserSimpleDto(user.getUsername(), user.getMmr()))
                .collect(Collectors.toList());
    }

    public ResponseMatchInfoDto getMatchHistoryInfo(Long matchInfoId) {

        MatchInfoEntity matchInfoEntity = matchInfoRepository.findById(matchInfoId)
                .orElseThrow(() -> new IllegalArgumentException("경기 정보 없음"));;
        MatchHistoryDto matchHistoryDto = convertToMatchHistoryDto(matchInfoEntity);

        List<CommentEntity> commentEntityList = matchInfoEntity.getCommentList().stream()
                .filter(comment -> comment.getDepth() == 0)
                .collect(Collectors.toList());
        Map<Long, String> userMap = preloadUsernames(commentEntityList);
        List<CommentDto> commentList = new ArrayList<>();
        for (CommentEntity commentEntity : commentEntityList) {
            CommentDto commetDto = convertToCommentDto(userMap, commentEntity);
            if (commetDto != null) {
                commentList.add(commetDto);
            }
        }
        return new ResponseMatchInfoDto(matchHistoryDto, commentList);
    }

    private CommentDto convertToCommentDto(Map<Long, String> userMap, CommentEntity commentEntity) {

        String username = userMap.get(commentEntity.getUserId());
        if (username == null) return null;

        CommentDto commentDto = new CommentDto();
        commentDto.setCommentId(commentEntity.getId());
        commentDto.setUserId(commentEntity.getUserId());
        commentDto.setUsername(username);
        commentDto.setDepth(commentEntity.getDepth());
        commentDto.setUpdatedAt(commentEntity.getUpdatedAt());
        commentDto.setContent(commentEntity.getContent());

        if (commentEntity.getChildList() != null && !commentEntity.getChildList().isEmpty()) {
            List<CommentDto> childDtoList = new ArrayList<>();
            for (CommentEntity childEntity : commentEntity.getChildList()) {
                CommentDto childDto = convertToCommentDto(userMap, childEntity);
                if (childDto != null) {
                    childDtoList.add(childDto);
                }
            }
            commentDto.setChildList(childDtoList);
        }

        return commentDto;
    }

    /**
     * UserEntity 조회 캐싱 (N+1 문제 방지)
     */
    private Map<Long, String> preloadUsernames(List<CommentEntity> comments) {
        List<Long> userIds = comments.stream()
                .map(CommentEntity::getUserId)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty()) return Collections.emptyMap();

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getUsername));
    }

    public void updateComment(Long userId, RequestUpdateCommentDto requestUpdateCommentDto) {

        if (requestUpdateCommentDto.getCommentId() != null) {       // 업데이트
            CommentEntity currentCommentEntity = commentRepository.findById(requestUpdateCommentDto.getCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("원댓글을 찾을 수 없습니다"));
            currentCommentEntity.updateContent(requestUpdateCommentDto.getContent());
            commentRepository.save(currentCommentEntity);
        } else {        // 인서트
            MatchInfoEntity matchInfoEntity = matchInfoRepository.findById(requestUpdateCommentDto.getMatchInfoId())
                    .orElseThrow(() -> new IllegalArgumentException("경기 정보 없음"));;
            CommentEntity commentEntity = CommentEntity.builder()
                    .content(requestUpdateCommentDto.getContent())
                    .matchInfo(matchInfoEntity)
                    .userId(userId)
                    .depth(0)
                    .build();
            if (requestUpdateCommentDto.getParentId() != null) {
                CommentEntity parentComment = commentRepository.findById(requestUpdateCommentDto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모댓글을 찾을 수 없습니다"));
                commentEntity.setParentComment(parentComment);
            }
            commentRepository.save(commentEntity);
        }
    }


}
