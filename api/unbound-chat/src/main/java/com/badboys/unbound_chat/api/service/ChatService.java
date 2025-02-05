package com.badboys.unbound_chat.api.service;

import com.badboys.unbound_chat.api.entity.ChatRoomEntity;
import com.badboys.unbound_chat.api.entity.ChatRoomUserEntity;
import com.badboys.unbound_chat.api.entity.UserEntity;
import com.badboys.unbound_chat.api.model.RequestCreateChatRoomDto;
import com.badboys.unbound_chat.api.repository.ChatRoomRepository;
import com.badboys.unbound_chat.api.repository.ChatRoomUserRepository;
import com.badboys.unbound_chat.api.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ChatService(UserRepository userRepository, ChatRoomRepository chatRoomRepository, ChatRoomUserRepository chatRoomUserRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomUserRepository = chatRoomUserRepository;
        this.modelMapper = modelMapper;
    }

    public void createChatRoom(RequestCreateChatRoomDto requestCreateChatRoomDto) {

        List<Long> userIdList = requestCreateChatRoomDto.getUserIdList()
                .stream()
                .map(Long::parseLong) // String → Long 변환
                .collect(Collectors.toList());
        List<UserEntity> users = userRepository.findAllById(userIdList);

        ChatRoomEntity chatRoom = ChatRoomEntity.builder()
                .build();
        chatRoomRepository.save(chatRoom);

        List<ChatRoomUserEntity> chatRoomUsers = new ArrayList<>();
        for (UserEntity user : users) {
            ChatRoomUserEntity chatRoomUser = ChatRoomUserEntity.builder()
                    .chatRoom(chatRoom)
                    .user(user)
                    .build();
            chatRoomUsers.add(chatRoomUser);
        }
        chatRoomUserRepository.saveAll(chatRoomUsers);

    }
}
