package com.badboys.unbound_chat.api.entity;

import com.badboys.unbound_chat.api.model.RoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "chat_member",
        indexes = {
                @Index(name = "idx_chat_member_id", columnList = "id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "chat_room_id"}) // 중복 방지
        }
)
public class ChatMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT 설정
    @Column(name = "id")
    private Long id;

    @Column(name = "lastReadMessageId")
    private String lastReadMessageId;

    @Column(name = "role") // 예: 'OWNER', 'MEMBER' 등
    private RoleType role;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
    @JoinColumn(name = "userId") // 외래 키 설정
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
    @JoinColumn(name = "chatRoomId") // 외래 키 설정
    private ChatRoomEntity chatRoom;

    @Column(name = "joined_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    public void upateLastReadMessage(String lastReadMessageId) {  // 마지막 읽은 채팅 업데이트
        this.lastReadMessageId = (lastReadMessageId != null) ? lastReadMessageId : this.lastReadMessageId;
    }
}
