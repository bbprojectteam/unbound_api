package com.badboys.unbound_chat.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "chat_room",
        indexes = {
                @Index(name = "idx_chat_room_id", columnList = "id")
        }
)
public class ChatRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT 설정
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "regionId")
    private Long regionId;

    @Column(name = "description")
    private String description;

    @ManyToMany
    @JoinTable(
            name = "chat_user",
            joinColumns = @JoinColumn(name = "chatRoomId"),
            inverseJoinColumns = @JoinColumn(name = "userId")
    )
    private List<UserEntity> userList;
}
