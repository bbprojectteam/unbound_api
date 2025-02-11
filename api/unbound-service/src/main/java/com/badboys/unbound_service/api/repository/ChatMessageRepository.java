package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {

    List<ChatMessageDocument> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}
