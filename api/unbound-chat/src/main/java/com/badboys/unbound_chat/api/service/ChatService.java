package com.badboys.unbound_chat.api.service;

import com.badboys.unbound_chat.api.entity.ChatRoomEntity;
import com.badboys.unbound_chat.api.entity.RegionEntity;
import com.badboys.unbound_chat.api.entity.UserEntity;
import com.badboys.unbound_chat.api.model.MatchSuccess;
import com.badboys.unbound_chat.api.repository.ChatRoomRepository;
import com.badboys.unbound_chat.api.repository.RegionRepository;
import com.badboys.unbound_chat.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {

    private final FcmService fcmService;

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Autowired
    public ChatService(FcmService fcmService, UserRepository userRepository, RegionRepository regionRepository, ChatRoomRepository chatRoomRepository) {
        this.fcmService = fcmService;
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.chatRoomRepository = chatRoomRepository;
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
                .userList(new ArrayList<>())
                .name(region.getName() != null ? region.getName() + " 채팅방" : "기본 채팅방")
                .regionId(region.getId())
                .build();
        chatRoom.getUserList().addAll(users);
        chatRoomRepository.save(chatRoom);

        fcmService.sendNotifications(userIdSet, "매칭 성공!", "새로운 게임을 즐겨보세요.",
                Map.of("chatRoomId", chatRoom.getId().toString(), "createdAt", LocalDateTime.now().toString()));
    }
}
