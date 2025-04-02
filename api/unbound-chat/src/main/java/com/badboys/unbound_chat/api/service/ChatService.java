package com.badboys.unbound_chat.api.service;

import com.badboys.unbound_chat.api.entity.*;
import com.badboys.unbound_chat.api.model.ChatMessage;
import com.badboys.unbound_chat.api.model.MatchSuccess;
import com.badboys.unbound_chat.api.model.RoleType;
import com.badboys.unbound_chat.api.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public ChatService(FcmService fcmService, UserRepository userRepository, RegionRepository regionRepository, ChatMemberRepository chatMemberRepository, ChatRoomRepository chatRoomRepository,
                       ChatMessageRepository chatMessageRepository, SimpMessagingTemplate messagingTemplate, KafkaTemplate<String, Object> kafkaTemplate) {

        this.fcmService = fcmService;
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

    public void sendMessage(ChatMessage chatMessage) {

        ChatMessageDocument chatMessageDocument = ChatMessageDocument.builder()
                .chatRoomId(chatMessage.getChatRoomId())
                .senderId(chatMessage.getSenderId())
                .message(chatMessage.getMessage())
                .createdAt(LocalDateTime.now())
                .build();
        chatMessageRepository.save(chatMessageDocument);

        try {
            // WebSocket을 통해 메시지 브로드캐스트
            messagingTemplate.convertAndSend("/topic/chat/" + chatMessage.getChatRoomId(), chatMessage,
                    messageHeaders(chatMessage.getSenderId().toString()));
            log.info("WebSocket으로 메시지 전송 완료: {}", chatMessage);
        } catch (Exception e) {
            log.error("WebSocket 메시지 전송 실패: {}", e.getMessage());
        }
    }

    private Map<String, Object> messageHeaders(String senderId) {

        Map<String, Object> headers = new HashMap<>();
        headers.put("exclude-sender", senderId);  // 메세지 보낸 유저를 WebSocket에서 제외하기 위한 커스텀 헤더 추가
        return headers;
    }
}
