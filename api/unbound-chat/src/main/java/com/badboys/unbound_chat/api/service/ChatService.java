package com.badboys.unbound_chat.api.service;

import com.badboys.unbound_chat.api.entity.*;
import com.badboys.unbound_chat.api.model.*;
import com.badboys.unbound_chat.api.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatService {

    private final FcmService fcmService;
    private final MongoService mongoService;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public ChatService(FcmService fcmService, MongoService mongoService, RedisService redisService, UserRepository userRepository, RegionRepository regionRepository, ChatMemberRepository chatMemberRepository, ChatRoomRepository chatRoomRepository,
                       ChatMessageRepository chatMessageRepository, SimpMessagingTemplate messagingTemplate, KafkaTemplate<String, Object> kafkaTemplate) {

        this.fcmService = fcmService;
        this.mongoService = mongoService;
        this.redisService = redisService;
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void createChatRoom(MatchSuccess matchSuccess) {

        Set<Long> userIdSet = new HashSet<>(matchSuccess.getUserIdSet());
        List<UserEntity> users = userRepository.findAllById(userIdSet);
        Set<Long> regionIdSet = matchSuccess.getRegionIdSet();
        List<RegionEntity> regions = regionRepository.findAllById(regionIdSet);
        RegionEntity region = regions.stream()
                .max(Comparator.comparingInt(RegionEntity::getDepth))
                .orElseThrow(() -> new IllegalArgumentException("유효한 지역 정보를 찾을 수 없습니다."));

        ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                .name(region.getName() != null ? region.getName() + " 라커룸" : "기본 라커룸")
                .regionId(region.getId())
                .build();

        chatRoomRepository.save(chatRoom);

        // MMR이 가장 높은 유저 찾기
        UserEntity owner = users.stream()
                .max(Comparator.comparingInt(UserEntity::getMmr))
                .orElseThrow(() -> new IllegalArgumentException("유효한 유저 정보가 없습니다."));

        List<ChatMemberEntity> chatMembers = users.stream()
                .map(user -> ChatMemberEntity.builder()
                        .chatRoom(chatRoom)
                        .user(user)
                        .joinedAt(LocalDateTime.now())
                        .role(user.equals(owner) ? RoleType.OWNER : RoleType.MEMBER) // 가장 높은 MMR이 OWNER
                        .build())
                .collect(Collectors.toList());

        chatMemberRepository.saveAll(chatMembers);

        fcmService.sendNotifications(userIdSet, "매칭 성공!", "새로운 게임을 즐겨보세요.",
                Map.of("chatRoomId", chatRoom.getId().toString(), "createdAt", LocalDateTime.now().toString()));
    }

    public void publishMessage(ChatMessage chatMessage) {

        kafkaTemplate.send("chat-message-topic", chatMessage)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        log.info("Kafka 메시지 전송 성공: {}", chatMessage);
                    } else {
                        log.error("Kafka 메시지 전송 실패: {}", exception.getMessage());
                    }
                });
    }

    @Transactional
    public void sendMessage(ChatMessage chatMessage) {

        Long nextMessageId = mongoService.getNextSequence("chatMessageId:room:" + chatMessage.getChatRoomId());

        ChatMessageDocument chatMessageDocument = ChatMessageDocument.builder()
                .chatRoomId(chatMessage.getChatRoomId())
                .chatMessageId(nextMessageId)
                .senderId(chatMessage.getSenderId())
                .message(chatMessage.getMessage())
                .imageUrl(chatMessage.getImageUrl())
                .createdAt(LocalDateTime.now())
                .build();
        chatMessageRepository.save(chatMessageDocument);

        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(chatMessage.getChatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        List<ChatMemberEntity> chatMemberEntityList = chatRoomEntity.getChatMemberList();

        Set<Long> userIds = new HashSet<>();

        for (ChatMemberEntity chatMemberEntity : chatMemberEntityList) {
            userIds.add(chatMemberEntity.getUser().getId());
        }

        String body = chatMessage.getImageUrl() != null
                ? "📷 사진을 보냈습니다."
                : chatMessage.getMessage();

        chatMessage.setChatMessageId(nextMessageId);

        try {
            // WebSocket을 통해 메시지 브로드캐스트
//            messagingTemplate.convertAndSend("/topic/chat/" + chatMessage.getChatRoomId(), chatMessage,
//                    messageHeaders(chatMessage.getSenderId().toString()));

            log.info("WebSocket으로 메시지 전송 완료: {}", chatMessage);
        } catch (Exception e) {
            log.error("WebSocket 메시지 전송 실패: {}", e.getMessage());
        }

        try {
            // fcm 채팅알림
            fcmService.sendNotifications(userIds, chatRoomEntity.getName(), body,
                    Map.of("chatRoomId", chatRoomEntity.getId().toString(), "createdAt", LocalDateTime.now().toString()));

            log.info("fcm 알림 전송 완료: {}", chatMessage);
        } catch (Exception e) {
            log.error("fcm 알림 전송 실패: {}", e.getMessage());
        }
    }

    private Map<String, Object> messageHeaders(String senderId) {

        Map<String, Object> headers = new HashMap<>();
        headers.put("exclude-sender", senderId);  // 메세지 보낸 유저를 WebSocket에서 제외하기 위한 커스텀 헤더 추가
        return headers;
    }

    public void processReadMessage(ReadMessage readMessage) {
        Long userId = readMessage.getUserId();
        Long chatRoomId = readMessage.getChatRoomId();
        Long lastReadMessageId = readMessage.getChatMessageId();

        // 1. Redis에 저장
        redisService.saveLastReadMessage(chatRoomId, userId, lastReadMessageId);

        // 2. rdb 챗멤버 업데이트
        ChatMemberEntity chatMemberEntity = chatMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        chatMemberEntity.upateLastReadMessage(lastReadMessageId.toString());
        chatMemberRepository.save(chatMemberEntity);

        // 3. 참여자들의 마지막 읽은 메시지 조회
        Map<Long, Long> userReadMap = redisService.getAllUserReadStatus(chatRoomId);

        // 4. 최근 100개 메시지 목록 조회
        PageRequest pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ChatMessageDocument> messages = chatMessageRepository.findByChatRoomId(chatRoomId, pageable);

        // 6. 메시지별 읽은 유저 수 계산
        Map<Long, Integer> readCounts = new HashMap<>(); // chatMessageId → count
        for (ChatMessageDocument msg : messages) {
            int count = 0;
            Long messageId = msg.getChatMessageId();
            for (Long otherUserRead : userReadMap.values()) {
                if (otherUserRead >= messageId) {
                    count++;
                }
            }
            readCounts.put(messageId, count);
        }

        // 5. WebSocket으로 읽음 수 브로드캐스트
        messagingTemplate.convertAndSend(
                "/topic/chat/read/" + chatRoomId,
                new ReadCount(readCounts)
        );
    }
}
