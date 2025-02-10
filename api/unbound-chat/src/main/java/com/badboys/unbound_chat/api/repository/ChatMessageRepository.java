package com.badboys.unbound_chat.api.repository;

import com.badboys.unbound_chat.api.entity.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {

}
