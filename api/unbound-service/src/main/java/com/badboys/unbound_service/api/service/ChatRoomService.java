package com.badboys.unbound_service.api.service;


import com.badboys.unbound_service.api.repository.ChatMemberRepository;
import com.badboys.unbound_service.api.repository.ChatMessageRepository;
import com.badboys.unbound_service.api.repository.ChatRoomRepository;
import com.badboys.unbound_service.entity.*;
import com.badboys.unbound_service.model.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatRoomService {

    private final UserService userService;

    private final RegionService regionService;

    private final ModelMapper modelMapper;

    private final ChatRoomRepository chatRoomRepository;

    private final ChatMemberRepository chatMemberRepository;

    private final ChatMessageRepository chatMessageRepository;

    private final S3Service s3Service;

    @Autowired
    public ChatRoomService(UserService userService, RegionService regionService, ModelMapper modelMapper, ChatRoomRepository chatRoomRepository, ChatMemberRepository chatMemberRepository, ChatMessageRepository chatMessageRepository, S3Service s3Service) {
        this.userService = userService;
        this.regionService = regionService;
        this.modelMapper = modelMapper;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.s3Service = s3Service;
    }

    public List<ChatRoomThumbnailDto> getJoinedChatRoomList(Long userId) {

        List<ChatRoomThumbnailDto> chatRoomList = chatRoomRepository.findJoinedChatRoomList(userId);

        for (ChatRoomThumbnailDto chatRoom : chatRoomList) {
            Long chatRoomId = chatRoom.getChatRoomId();

            ChatMemberEntity chatMemberEntity = chatMemberRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
            if (chatMemberEntity == null) continue;

            String lastReadMessageId = chatMemberEntity.getLastReadMessageId();
            int unreadCount = (lastReadMessageId == null)
                    ? chatMessageRepository.countByChatRoomId(chatRoomId) // 모든 메시지 개수
                    : chatMessageRepository.countByChatRoomIdAndIdGreaterThan(chatRoomId, new ObjectId(lastReadMessageId)); // 읽지 않은 메시지 개수

            ChatMessageDocument lastMessage = chatMessageRepository.findTopByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
            String lastMessageText = (lastMessage != null) ? lastMessage.getMessage() : null;
            String lastMessageTime = (lastMessage != null) ? lastMessage.getCreatedAt().toString() : null;

            chatRoom.setLastMessage(lastMessageText);
            chatRoom.setLastMessageCreatedAt(lastMessageTime);
            chatRoom.setUnreadCnt(unreadCount);
        }

        return chatRoomList;
    }

    public List<ChatRoomThumbnailDto> getUnJoinedChatRoomList(Long userId, Long regionId) {

        List<Long> regionIdList = regionService.getAllChildrenId(regionId);
        List<ChatRoomThumbnailDto> chatRoomThumbnailDtoList = chatRoomRepository.findUnjoinedChatRoomList(userId, regionIdList);
        return chatRoomThumbnailDtoList;
    }

    public ResponseChatRoomInfoDto getChatRoomInfo(Long userId, Long chatRoomId) {

        ResponseChatRoomInfoDto responseDto = new ResponseChatRoomInfoDto();

        ChatRoomInfo chatRoomInfo = chatRoomRepository.findChatRoomInfo(userId, chatRoomId);
        List<ChatMemberDto> memberList = chatMemberRepository.findChatMembersByChatRoomId(chatRoomId);

        responseDto.setChatRoomInfo(chatRoomInfo);
        responseDto.setMemberList(memberList);
        return responseDto;
    }

    @Transactional // 읽음처리 + 메세지 목록 반환
    public ResponseMessageListDto getMessages(Long userId, Long chatRoomId, String lastMessageId) {

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

        int messageCnt = chatMessageRepository.countByChatRoomId(chatRoomId);
        List<MessageDto> messageDtoList = convertMessage(messages, chatRoomId);

        return new ResponseMessageListDto(messageDtoList, messageCnt);       // 메시지 목록 반환
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

    public void updateChatRoomInfo(Long chatRoomId, RequestUpdateChatRoomDto requestUpdateChatRoomDto){

        ChatRoomEntity chatRoomEntity = getChatRoomEntity(chatRoomId);
        chatRoomEntity.updateChatRoomInfo(
                requestUpdateChatRoomDto.getName(),
                requestUpdateChatRoomDto.getLocation(),
                requestUpdateChatRoomDto.getDescription(),
                requestUpdateChatRoomDto.getMatchDt(),
                requestUpdateChatRoomDto.getThreeOnThreeYn(),
                requestUpdateChatRoomDto.getBallYn(),
                requestUpdateChatRoomDto.getRefereeYn(),
                requestUpdateChatRoomDto.getBackBoardYn(),
                requestUpdateChatRoomDto.getThreePointLimitYn(),
                requestUpdateChatRoomDto.getHalfCourtYn(),
                requestUpdateChatRoomDto.getLatitude(),
                requestUpdateChatRoomDto.getLongitude()
        );
        chatRoomRepository.save(chatRoomEntity);
    }

    public ChatRoomEntity getChatRoomEntity(Long chatRoomId) {
        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다. ID: " + chatRoomId));
        return chatRoomEntity;
    }

    @Transactional
    public void exitChatRoom(Long userId, Long chatRoomId){

        ChatMemberEntity chatMemberEntity = chatMemberRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
        if (chatMemberEntity == null) {
            throw new EntityNotFoundException("채팅방에 해당 유저가 존재하지 않습니다.");
        }
        ChatRoomEntity chatRoomEntity = chatMemberEntity.getChatRoom();

        chatRoomEntity.removeChatMember(chatMemberEntity);
        chatRoomRepository.save(chatRoomEntity);
    }

    @Transactional
    public boolean joinChatRoom(Long userId, Long chatRoomId){

        ChatRoomEntity chatRoom = getChatRoomEntity(chatRoomId);
        List<ChatMemberEntity> chatMemberList = chatRoom.getChatMemberList();

        if (chatMemberList.size() >= 6) {
            return false;
        }
        UserEntity user = userService.getUserEntity(userId);
        ChatMemberEntity newMember =  ChatMemberEntity.builder()
                .chatRoom(chatRoom)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .role(RoleType.MEMBER)
                .build();
        chatRoom.addChatMember(newMember);
        chatRoomRepository.save(chatRoom);

        return true;
    }

    public String uploadImage(MultipartFile imageFile) {

        String imageUrl = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageUrl = s3Service.uploadFile(imageFile);
            } catch (RuntimeException e) {
                throw new RuntimeException("이미지 업로드 중 오류 발생", e);
            }
        }

        return imageUrl;
    }

    @Transactional
    public void changeOwner(Long userId, Long chatRoomId, Long targetUserId){

        ChatMemberEntity ownerMember = chatMemberRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
        ChatMemberEntity targetMember = chatMemberRepository.findByUserIdAndChatRoomId(targetUserId, chatRoomId);
        if (ownerMember == null) {
            throw new EntityNotFoundException("채팅방에 해당 유저가 존재하지 않습니다.");
        }
        if (targetMember == null) {
            throw new EntityNotFoundException("채팅방에 해당 유저가 존재하지 않습니다.");
        }
        RoleType role = ownerMember.getRole();

        if (!role.equals(RoleType.OWNER)) {
            throw new EntityNotFoundException("권한이 없습니다.");
        }

        ownerMember.updateRole(RoleType.MEMBER);
        targetMember.updateRole(RoleType.OWNER);

        chatMemberRepository.save(ownerMember);
        chatMemberRepository.save(targetMember);
    }
}
