package com.badboys.unbound_chat.api.repository;

import com.badboys.unbound_chat.api.entity.ChatMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMemberRepository extends JpaRepository<ChatMemberEntity, Long> {
}
