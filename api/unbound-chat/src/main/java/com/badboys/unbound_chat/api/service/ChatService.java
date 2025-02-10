package com.badboys.unbound_chat.api.service;

import com.badboys.unbound_chat.api.entity.ChatRoomEntity;
import com.badboys.unbound_chat.api.entity.RegionEntity;
import com.badboys.unbound_chat.api.entity.UserEntity;
import com.badboys.unbound_chat.api.model.RequestCreateChatRoomDto;
import com.badboys.unbound_chat.api.repository.ChatRoomRepository;
import com.badboys.unbound_chat.api.repository.RegionRepository;
import com.badboys.unbound_chat.api.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Autowired
    public ChatService(UserRepository userRepository, RegionRepository regionRepository, ChatRoomRepository chatRoomRepository) {
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    public void createChatRoom(RequestCreateChatRoomDto requestCreateChatRoomDto) {

        Set<Long> userIdList = new HashSet<>(requestCreateChatRoomDto.getUserIdSet());
        List<UserEntity> users = Optional.of(userRepository.findAllById(userIdList))
                .orElse(Collections.emptyList());
        Set<Long> regionIdList = requestCreateChatRoomDto.getRegionIdSet();
        List<RegionEntity> regions = regionRepository.findAllById(regionIdList);
        RegionEntity region = regions.stream()
                .max(Comparator.comparingInt(RegionEntity::getDepth))  // 가장 깊은 값 찾기
                .orElse(null);

        if (users.size() == 6) {
            ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                    .userList(new ArrayList<>())
                    .name(region.getName())
                    .regionId(region.getId())
                    .build();
            chatRoom.getUserList().addAll(users);
            chatRoomRepository.save(chatRoom);
        }

        // Todo 유저들에게 매칭성공 알림
    }
}
