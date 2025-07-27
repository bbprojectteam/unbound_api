package com.badboys.unbound_service.api.service;

import com.badboys.unbound_service.api.repository.*;
import com.badboys.unbound_service.entity.*;
import com.badboys.unbound_service.model.*;
import com.badboys.unbound_service.util.MmrUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchService {

    private final UserService userService;
    private final RegionService regionService;
    private final UserRepository userRepository;
    private final MatchInfoRepository matchInfoRepository;
    private final CommentRepository commentRepository;
    private final TeamRepository teamRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public MatchService(UserService userService, RegionService regionService, UserRepository userRepository, MatchInfoRepository matchInfoRepository,
                        CommentRepository commentRepository, TeamRepository teamRepository, ChatRoomRepository chatRoomRepository,
                        KafkaTemplate<String, Object> kafkaTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.regionService = regionService;
        this.userRepository = userRepository;
        this.matchInfoRepository = matchInfoRepository;
        this.commentRepository = commentRepository;
        this.teamRepository = teamRepository;
        this.chatRoomRepository = chatRoomRepository;
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

            List<Long> regionRange = regionService.getAllChildrenId(limitRegionId);
            int mmr = userEntity.getMmr();

            RequestMatchDto requestMatchDto = new RequestMatchDto(userId, mmr, regionRange, limitRegionId);

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

    public List<MatchInfoDto> getUserMatchInfoList(Long userId) {

        Page<MatchInfoEntity> page = matchInfoRepository.findByUserId(userId, PageRequest.of(0, 5));

        List<MatchInfoDto> userMatchInfoList = page.stream()
                .map(match -> {
                    MatchInfoDto dto = convertToMatchInfoDto(match);

                    int mmrChange = 0;
                    Long myTeamId = null;

                    for (TeamEntity team : match.getTeamList()) {
                        for (TeamUserEntity tu : team.getTeamUsers()) {
                            if (tu.getUser().getId().equals(userId)) {
                                mmrChange = tu.getMmrChange();
                                myTeamId = team.getId();
                                break;
                            }
                        }
                        if (myTeamId != null) break;
                    }

                    dto.setMmrChange(mmrChange);

                    if (myTeamId != null) {
                        List<TeamInfoDto> teams = dto.getTeamList();
                        for (int i = 0; i < teams.size(); i++) {
                            if (teams.get(i).getTeamId().equals(myTeamId)) {
                                TeamInfoDto myTeam = teams.remove(i);
                                teams.add(0, myTeam);
                                break;
                            }
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        return userMatchInfoList;
    }

    public List<MatchInfoDto> getRegionMatchInfoList(Long regionId) {

        Page<MatchInfoEntity> regionMatchInfoEntityList = matchInfoRepository.findByRegionId(regionId, PageRequest.of(0, 5));
        List<MatchInfoDto> regionMatchInfoList = regionMatchInfoEntityList.stream()
                .map(this::convertToMatchInfoDto)
                .collect(Collectors.toList());

        return regionMatchInfoList;
    }

    private MatchInfoDto convertToMatchInfoDto(MatchInfoEntity matchInfo) {
        List<TeamInfoDto> teamList = convertToTeamInfoDto(matchInfo.getTeamList());

        return new MatchInfoDto(
                matchInfo.getId(),
                matchInfo.getMatchName(),
                matchInfo.getStartAt(),
                matchInfo.getEndAt(),
                matchInfo.getRegion().getId(),
                matchInfo.getLatitude(),
                matchInfo.getLongitude(),
                matchInfo.getLocation(),
                0,
                teamList
        );
    }

    private List<TeamInfoDto> convertToTeamInfoDto(Set<TeamEntity> teamEntities) {
        List<TeamInfoDto> result = new ArrayList<>();

        for (TeamEntity team : teamEntities) {
            Set<UserEntity> userSet = new LinkedHashSet<>();
            for (TeamUserEntity tu : team.getTeamUsers()) {
                userSet.add(tu.getUser());
            }

            List<UserSimpleDto> userList = userService.convertToUserSimpleDto(userSet);

            TeamInfoDto dto = new TeamInfoDto(team.getId(), team.getResult(), userList);
            result.add(dto);
        }

        return result;
    }

    public ResponseMatchInfoDto getMatchInfo(Long matchInfoId) {

        MatchInfoEntity matchInfoEntity = matchInfoRepository.findById(matchInfoId)
                .orElseThrow(() -> new IllegalArgumentException("경기 정보 없음"));;
        MatchInfoDto matchInfoDto = convertToMatchInfoDto(matchInfoEntity);

        List<CommentDto> flatCommentList = commentRepository.findAllByMatchInfoWithUser(matchInfoId);

        List<CommentDto> commentTree = buildCommentTree(flatCommentList);
        return new ResponseMatchInfoDto(matchInfoDto, commentTree);
    }

    public List<CommentDto> buildCommentTree(List<CommentDto> flatList) {
        Map<Long, CommentDto> commentMap = flatList.stream()
                .collect(Collectors.toMap(CommentDto::getCommentId, Function.identity()));

        List<CommentDto> rootList = new ArrayList<>();

        for (CommentDto comment : flatList) {
            if (comment.getParentId() == null || comment.getDepth() == 0) {
                rootList.add(comment);
            } else {
                CommentDto parent = commentMap.get(comment.getParentId());
                if (parent != null) {
                    parent.getChildList().add(comment);
                }
            }
        }

        return rootList;
    }

    public void updateComment(Long userId, RequestUpdateCommentDto requestUpdateCommentDto) {

        if (requestUpdateCommentDto.getCommentId() != null) {       // 업데이트
            CommentEntity currentCommentEntity = commentRepository.findById(requestUpdateCommentDto.getCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("원댓글을 찾을 수 없습니다"));
            if (requestUpdateCommentDto.getUseYn().equals("N")) {
                currentCommentEntity.deleteComment();
            }
            else {
                currentCommentEntity.updateContent(requestUpdateCommentDto.getContent());
            }
            commentRepository.save(currentCommentEntity);
        } else {        // 인서트
            MatchInfoEntity matchInfoEntity = matchInfoRepository.findById(requestUpdateCommentDto.getMatchInfoId())
                    .orElseThrow(() -> new IllegalArgumentException("경기 정보 없음"));

            UserEntity userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다: " + userId));

            CommentEntity commentEntity = CommentEntity.builder()
                    .content(requestUpdateCommentDto.getContent())
                    .useYn("Y")
                    .matchInfo(matchInfoEntity)
                    .user(userEntity)
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

    @Transactional
    public ResponseGameStartDto startGame(RequestGameStartDto requestGameStartDto) {

        RegionEntity regionEntity = regionService.getRegion(requestGameStartDto.getRegionId());

        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(requestGameStartDto.getChatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방 정보 없음"));

        MatchInfoEntity matchInfo = MatchInfoEntity.builder()
                .startAt(LocalDateTime.now())
                .matchName(requestGameStartDto.getMatchName())
                .region(regionEntity)
                .location(chatRoomEntity.getLocation())
                .latitude(chatRoomEntity.getLatitude())
                .longitude(chatRoomEntity.getLongitude())
                .build();

        List<UserEntity> aTeamUsers = userRepository.findAllById(requestGameStartDto.getATeamIdList());
        List<UserEntity> bTeamUsers = userRepository.findAllById(requestGameStartDto.getBTeamIdList());

        TeamEntity aTeam = TeamEntity.builder()
                .result(null)
                .matchInfo(matchInfo)
                .build();

        TeamEntity bTeam = TeamEntity.builder()
                .result(null)
                .matchInfo(matchInfo)
                .build();

        for (UserEntity user : aTeamUsers) {
            TeamUserEntity teamUser = TeamUserEntity.builder()
                    .team(aTeam)
                    .user(user)
                    .build();
            aTeam.getTeamUsers().add(teamUser);
        }

        for (UserEntity user : bTeamUsers) {
            TeamUserEntity teamUser = TeamUserEntity.builder()
                    .team(bTeam)
                    .user(user)
                    .build();
            bTeam.getTeamUsers().add(teamUser);
        }

        matchInfo.getTeamList().add(aTeam);
        matchInfo.getTeamList().add(bTeam);

        matchInfoRepository.save(matchInfo);

        MatchInfoEntity matchInfoEntity = matchInfoRepository.findById(matchInfo.getId())
                .orElseThrow(() -> new IllegalArgumentException("경기 정보 없음"));

        ResponseGameStartDto responseGameStartDto = new ResponseGameStartDto(matchInfoEntity.getId(), aTeam.getId(), bTeam.getId());
        return responseGameStartDto;
    }

    @Transactional
    public void endGame(RequestGameEndDto dto) {

        MatchInfoEntity matchInfo = matchInfoRepository.findByIdWithTeamsAndUsers(dto.getMatchInfoId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매치입니다"));

        TeamEntity aTeam = teamRepository.findById(dto.getATeamResult().getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("A팀이 존재하지 않습니다"));

        TeamEntity bTeam = teamRepository.findById(dto.getBTeamResult().getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("B팀이 존재하지 않습니다"));

        aTeam.updateResult(dto.getATeamResult().getScore(), dto.getATeamResult().getResult());
        bTeam.updateResult(dto.getBTeamResult().getScore(), dto.getBTeamResult().getResult());

        matchInfo.updateEndAt(LocalDateTime.now());

        updateMmrByResult(aTeam, bTeam);
    }

    private void updateMmrByResult(TeamEntity aTeam, TeamEntity bTeam) {
        int aAvg = calcAverageMmr(aTeam);
        int bAvg = calcAverageMmr(bTeam);

        double aScore = getActualScore(aTeam.getResult());
        double bScore = getActualScore(bTeam.getResult());

        for (TeamUserEntity teamUser : aTeam.getTeamUsers()) {
            UserEntity user = teamUser.getUser();
            int newMmr = MmrUtil.calculateNewRating(user.getMmr(), bAvg, aScore);
            teamUser.updateMmrChange(newMmr - user.getMmr());
            user.updateMmr(newMmr);

        }

        for (TeamUserEntity teamUser : bTeam.getTeamUsers()) {
            UserEntity user = teamUser.getUser();
            int newMmr = MmrUtil.calculateNewRating(user.getMmr(), aAvg, bScore);
            teamUser.updateMmrChange(newMmr - user.getMmr());
            user.updateMmr(newMmr);
        }
    }

    private int calcAverageMmr(TeamEntity team) {
        return (int) team.getTeamUsers().stream()
                .map(TeamUserEntity::getUser)
                .mapToInt(UserEntity::getMmr)
                .average()
                .orElse(1200);
    }

    private double getActualScore(MatchResultType result) {
        return switch (result) {
            case WIN -> 1.0;
            case DRAW -> 0.5;
            case LOSE -> 0.0;
        };
    }

}
