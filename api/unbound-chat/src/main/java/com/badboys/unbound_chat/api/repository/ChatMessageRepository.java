package com.badboys.unbound_chat.api.repository;

import com.badboys.unbound_chat.api.entity.ChatMessageDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {

    @Query("{ 'chatRoomId': ?0 }")
    List<ChatMessageDocument> findByChatRoomId(Long chatRoomId, Pageable pageable);
}
