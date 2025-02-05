package com.badboys.unbound_chat.api.repository;

import com.badboys.unbound_chat.api.entity.ChatRoomUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUserEntity, Long> {
}
