package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {

    List<ChatMessageDocument> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    // 특정 메시지 이후의 안 읽은 메시지 개수 조회
    int countByChatRoomIdAndIdGreaterThan(Long chatRoomId, String lastReadMessageId);

    // 채팅방의 마지막 메시지 조회
    ChatMessageDocument findTopByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);
}
