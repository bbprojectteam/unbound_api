package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    List<ChatRoomEntity> findAllByUserId(Long userId);
}
