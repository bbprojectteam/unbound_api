package com.badboys.unbound_chat.api.repository;

import com.badboys.unbound_chat.api.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {
}
