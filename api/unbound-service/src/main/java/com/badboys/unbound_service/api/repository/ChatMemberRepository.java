package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.ChatMemberEntity;
import com.badboys.unbound_service.entity.ChatRoomEntity;
import com.badboys.unbound_service.model.ChatMemberDto;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMemberEntity, Long> {

    @EntityGraph(attributePaths = {"chatMemberList"})
    @Query("SELECT cm.chatRoom FROM ChatMemberEntity cm WHERE cm.user.id = :userId")
    List<ChatRoomEntity> findChatRoomsByUserId(@Param("userId") Long userId);

    @Query("SELECT cm FROM ChatMemberEntity cm WHERE cm.user.id = :userId AND cm.chatRoom.id = :chatRoomId")
    ChatMemberEntity findByUserIdAndChatRoomId(@Param("userId") Long userId, @Param("chatRoomId") Long chatRoomId);

    @Query("SELECT new com.badboys.unbound_service.model.ChatMemberDto(cm.user.id, cm.user.username, cm.user.profileImage, cm.user.mmr, cm.lastReadMessageId) " +
            "FROM ChatMemberEntity cm " +
            "JOIN cm.user " +
            "WHERE cm.chatRoom.id = :chatRoomId AND cm.user.id != :userId")
    List<ChatMemberDto> findChatMembersByChatRoomIdExcludingUser(@Param("userId") Long userId, @Param("chatRoomId") Long chatRoomId);

    @Query("SELECT new com.badboys.unbound_service.model.ChatMemberDto(cm.user.id, cm.user.username, cm.user.profileImage, cm.user.mmr, cm.lastReadMessageId) " +
            "FROM ChatMemberEntity cm " +
            "JOIN cm.user " +
            "WHERE cm.chatRoom.id = :chatRoomId")
    List<ChatMemberDto> findChatMembersByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
