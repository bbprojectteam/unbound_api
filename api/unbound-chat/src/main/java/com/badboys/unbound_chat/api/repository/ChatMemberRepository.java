package com.badboys.unbound_chat.api.repository;

import com.badboys.unbound_chat.api.entity.ChatMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMemberRepository extends JpaRepository<ChatMemberEntity, Long> {

    ChatMemberEntity findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    List<ChatMemberEntity> findByChatRoomId(Long chatRoomId);
}
