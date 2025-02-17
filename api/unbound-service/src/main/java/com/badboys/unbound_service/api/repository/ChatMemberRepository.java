package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.ChatMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMemberRepository extends JpaRepository<ChatMemberEntity, Long> {

    ChatMemberEntity findByUserIdAndChatRoomId(Long userId, Long chatRoomId);
}
