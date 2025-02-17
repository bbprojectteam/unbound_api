package com.badboys.unbound_service.api.service;


import com.badboys.unbound_service.api.repository.ChatMemberRepository;
import com.badboys.unbound_service.api.repository.ChatMessageRepository;
import com.badboys.unbound_service.api.repository.ChatRoomRepository;
import com.badboys.unbound_service.entity.ChatMemberEntity;
import com.badboys.unbound_service.entity.ChatMessageDocument;
import com.badboys.unbound_service.entity.ChatRoomEntity;
import com.badboys.unbound_service.model.ChatRoomThumbnail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    private final ChatMemberRepository chatMemberRepository;

    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatRoomService(ChatRoomRepository chatRoomRepository, ChatMemberRepository chatMemberRepository, ChatMessageRepository chatMessageRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public List<ChatRoomThumbnail> getChatRoomList(Long userId) {

        List<ChatRoomThumbnail> chatRoomThumbnailList = new ArrayList<>();

        List<ChatRoomEntity> chatRoomEntityList = chatRoomRepository.findAllByUserId(userId);
        for (ChatRoomEntity chatRoomEntity : chatRoomEntityList) {

            Long chatRoomId = chatRoomEntity.getId();
            ChatMemberEntity chatMemberEntity = chatMemberRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
            int cnt = chatMessageRepository.countByChatRoomIdAndIdGreaterThan(chatRoomId, chatMemberEntity.getLastReadMessageId());
            ChatMessageDocument lastMessage = chatMessageRepository.findTopByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
            ChatRoomThumbnail chatRoomThumbnail = new ChatRoomThumbnail(chatRoomEntity.getName(), cnt, lastMessage.getMessage(), lastMessage.getCreatedAt().toString());
            chatRoomThumbnailList.add(chatRoomThumbnail);
        }

        return chatRoomThumbnailList;
    }

}
