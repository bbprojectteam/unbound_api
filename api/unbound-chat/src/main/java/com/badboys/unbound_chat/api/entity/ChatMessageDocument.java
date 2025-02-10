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

    private Long userId;

    private String message;

    private LocalDateTime createdAt;
}
