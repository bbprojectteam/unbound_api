package com.badboys.unbound_chat.api.service;

import com.badboys.unbound_chat.api.entity.ChatRoomEntity;
import com.badboys.unbound_chat.api.entity.UserEntity;
import com.badboys.unbound_chat.api.model.RequestCreateChatRoomDto;
import com.badboys.unbound_chat.api.repository.ChatRoomRepository;
import com.badboys.unbound_chat.api.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ChatService(UserRepository userRepository, ChatRoomRepository chatRoomRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.modelMapper = modelMapper;
    }

    public void createChatRoom(RequestCreateChatRoomDto requestCreateChatRoomDto) {

        List<Long> userIdList = requestCreateChatRoomDto.getUserIdList()
                .stream()
                .map(Long::parseLong) // String → Long 변환
                .collect(Collectors.toList());
        List<UserEntity> users = Optional.of(userRepository.findAllById(userIdList))
                .orElse(Collections.emptyList());

        if (users.size() == 6) {
            ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                    .userList(new ArrayList<>())
                    .build();
            chatRoom.getUserList().addAll(users);
            chatRoomRepository.save(chatRoom);
        }
    }
}
