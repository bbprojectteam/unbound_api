package com.badboys.unbound_chat.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "fcm_token",
        indexes = {
                @Index(name = "idx_token_id", columnList = "id"),
                @Index(name = "idx_user_id", columnList = "user_id", unique = true),
                @Index(name = "idx_token", columnList = "token", unique = true)
        }
)
public class FcmTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT 설정
    @Column(name = "id")
    private Long id;

    @Column(name = "token")
    private String token;

    @Column(name = "appId")
    private String appId;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
    @JoinColumn(name = "userId") // 외래 키 설정
    private UserEntity user;
}
