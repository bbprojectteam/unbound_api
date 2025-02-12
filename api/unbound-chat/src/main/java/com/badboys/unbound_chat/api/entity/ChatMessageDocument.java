package com.badboys.unbound_chat.api.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "chat_message")
public class ChatMessageDocument {

    @Id
    private String id;  // MongoDB는 기본적으로 String 타입의 _id 사용

    private Long chatRoomId;

    private Long senderId;

    private String message;

    private LocalDateTime createdAt;

    public ChatMessageDocument(Long chatRoomId, Long senderId, String message) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.message = message;
        this.createdAt = LocalDateTime.now(); // 메시지 생성 시간 자동 설정
    }
}
