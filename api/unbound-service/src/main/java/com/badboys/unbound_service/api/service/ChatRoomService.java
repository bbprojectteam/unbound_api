package com.badboys.unbound_service.api.service;


import com.badboys.unbound_service.api.repository.ChatMemberRepository;
import com.badboys.unbound_service.api.repository.ChatMessageRepository;
import com.badboys.unbound_service.api.repository.ChatRoomRepository;
import com.badboys.unbound_service.entity.ChatMemberEntity;
import com.badboys.unbound_service.entity.ChatMessageDocument;
import com.badboys.unbound_service.entity.ChatRoomEntity;
import com.badboys.unbound_service.model.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatRoomService {

    private final UserService userService;

    private final ModelMapper modelMapper;

    private final ChatRoomRepository chatRoomRepository;

    private final ChatMemberRepository chatMemberRepository;

    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatRoomService(UserService userService, ModelMapper modelMapper, ChatRoomRepository chatRoomRepository, ChatMemberRepository chatMemberRepository, ChatMessageRepository chatMessageRepository) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public List<ChatRoomThumbnailDto> getChatRoomList(Long userId) {

        List<ChatRoomThumbnailDto> chatRoomThumbnailDtoList = new ArrayList<>();

        List<ChatRoomEntity> chatRoomEntityList = chatMemberRepository.findChatRoomsByUserId(userId);

        for (ChatRoomEntity chatRoomEntity : chatRoomEntityList) {
            Long chatRoomId = chatRoomEntity.getId();

            ChatMemberEntity chatMemberEntity = chatMemberRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
            if (chatMemberEntity == null) continue;

            String lastReadMessageId = chatMemberEntity.getLastReadMessageId();
            int unreadCount = (lastReadMessageId == null)
                    ? chatMessageRepository.countByChatRoomId(chatRoomId) // 모든 메시지 개수
                    : chatMessageRepository.countByChatRoomIdAndIdGreaterThan(chatRoomId, new ObjectId(lastReadMessageId)); // 읽지 않은 메시지 개수

            ChatMessageDocument lastMessage = chatMessageRepository.findTopByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
            String lastMessageText = (lastMessage != null) ? lastMessage.getMessage() : null;
            String lastMessageTime = (lastMessage != null) ? lastMessage.getCreatedAt().toString() : null;

            chatRoomThumbnailDtoList.add(new ChatRoomThumbnailDto(chatRoomEntity.getId(), chatRoomEntity.getName(), unreadCount, lastMessageText, lastMessageTime));
        }

        return chatRoomThumbnailDtoList;
    }

    public ResponseChatRoomInfoDto getChatRoomInfo(Long userId, Long chatRoomId) {

        ResponseChatRoomInfoDto responseDto = new ResponseChatRoomInfoDto();

        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다. ID: " + chatRoomId));
        ChatRoomInfo chatRoomInfo = modelMapper.map(chatRoomEntity, ChatRoomInfo.class);

        List<ChatMemberDto> memberList = chatMemberRepository.findChatMembersByChatRoomIdExcludingUser(userId, chatRoomId);

        responseDto.setChatRoomInfo(chatRoomInfo);
        responseDto.setMemberList(memberList);
        return responseDto;
    }

    @Transactional // 읽음처리 + 메세지 목록 반환
    public List<MessageDto> getMessages(Long userId, Long chatRoomId, String lastMessageId) {

        if (lastMessageId == null) {  // 채팅방 진입시
            readMessage(userId, chatRoomId);  // 읽음 처리
        }

        List<ChatMessageDocument> messages;
        if (lastMessageId == null) {
            messages = chatMessageRepository.findTop20ByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
        }
        // lastMessageId가 있으면 해당 메시지보다 오래된 메시지 20개 가져오기
        else {
            messages = chatMessageRepository.findTop20ByChatRoomIdAndIdLessThanOrderByCreatedAtDesc(chatRoomId, new ObjectId(lastMessageId));
        }

        return convertMessage(messages, chatRoomId);       // 메시지 목록 반환
    }

    // 메시지 읽음 처리 (lastReadMessageId 업데이트)
    @Transactional
    public void readMessage(Long userId, Long chatRoomId) {
        ChatMemberEntity chatMember = chatMemberRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
        if (chatMember == null) return;

        // 채팅방의 마지막 메시지 가져오기
        ChatMessageDocument lastMessage = chatMessageRepository.findTopByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
        if (lastMessage != null) {
            chatMember.upateLastReadMessage(lastMessage.getId()); // 마지막 메시지 ID 업데이트
            chatMemberRepository.save(chatMember);
        }
    }

    // 채팅방의 메시지 목록 반환
    public List<MessageDto> convertMessage(List<ChatMessageDocument> messages, Long chatRoomId) {

        List<ChatMemberDto> chatMembers = chatMemberRepository.findChatMembersByChatRoomId(chatRoomId);
        Map<Long, ChatMemberDto> memberMap = new HashMap<>();
        List<ObjectId> memberLastReadIdList = getObjectIds(chatMembers, memberMap);

        List<MessageDto> responseMessages = new ArrayList<>();
        for (ChatMessageDocument messageDocument : messages) {

            int unreadMemberCnt = compareReadCnt(memberMap.size(), memberLastReadIdList, new ObjectId(messageDocument.getId()));
            ChatMemberDto member = memberMap.get(messageDocument.getSenderId());
            MessageDto messageDto = new MessageDto(messageDocument.getId(), messageDocument.getSenderId(), member.getUsername(), member.getProfileImage(),
                    messageDocument.getMessage(), messageDocument.getCreatedAt().toString(), unreadMemberCnt);
            responseMessages.add(messageDto);
        }

        return responseMessages;
    }

    private static List<ObjectId> getObjectIds(List<ChatMemberDto> chatMembers, Map<Long, ChatMemberDto> memberMap) {
        List<ObjectId> memberLastReadIdList = new ArrayList<>();
        for (ChatMemberDto chatMember : chatMembers) {
            String lastReadMessageId = chatMember.getLastReadMessageId();
            ObjectId lastReadMessageObjectId = null;

            if (lastReadMessageId != null) {
                lastReadMessageObjectId = new ObjectId(lastReadMessageId);
                memberLastReadIdList.add(lastReadMessageObjectId);
            }

            memberMap.put(chatMember.getUserId(), chatMember);
        }
        return memberLastReadIdList;
    }

    private int compareReadCnt(int memberCnt, List<ObjectId> memberLastReadIdList, ObjectId messageId) {

        int unreadMemberCnt = memberCnt;
        for (ObjectId lastReadId : memberLastReadIdList) {
            if (lastReadId != null && lastReadId.compareTo(messageId) >= 0) {  // 읽은 메시지가 현재 메시지보다 작은 경우 (즉, 아직 안 읽음)
                unreadMemberCnt--;
            }
        }

        return unreadMemberCnt;  // 안 읽은 멤버 수 반환
    }

    @Transactional // 읽음처리 + 메세지 목록 반환
    public List<MessageDto> getRefreshMessages(Long userId, Long chatRoomId, String lastMessageId) {

        if (lastMessageId == null) {  // 채팅방 진입시
            readMessage(userId, chatRoomId);  // 읽음 처리
        }
        List<ChatMessageDocument> messages = chatMessageRepository.findAllByChatRoomIdAndIdLessThanOrderByCreatedAtDesc(chatRoomId, new ObjectId(lastMessageId));

        return convertMessage(messages, chatRoomId);       // 메시지 목록 반환
    }




}
