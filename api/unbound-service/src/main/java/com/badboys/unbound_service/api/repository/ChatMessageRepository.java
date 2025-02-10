package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {

    List<ChatMessageDocument> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}
