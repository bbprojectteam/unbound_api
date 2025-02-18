package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.ChatMessageDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {

    List<ChatMessageDocument> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    int countByChatRoomId(Long chatRoomId);

    // 특정 메시지 이후의 안 읽은 메시지 개수 조회
    int countByChatRoomIdAndIdGreaterThan(Long chatRoomId, ObjectId lastReadMessageId);

    // 채팅방의 마지막 메시지 조회
    ChatMessageDocument findTopByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);

    List<ChatMessageDocument> findTop20ByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);

    List<ChatMessageDocument> findTop20ByChatRoomIdAndIdLessThanOrderByCreatedAtDesc(Long chatRoomId, ObjectId lastMessageId);

    List<ChatMessageDocument> findAllByChatRoomIdAndIdLessThanOrderByCreatedAtDesc(Long chatRoomId, ObjectId lastMessageId);
}
