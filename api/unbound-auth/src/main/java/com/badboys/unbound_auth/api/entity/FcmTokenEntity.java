package com.badboys.unbound_auth.api.entity;

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
        name = "fcm_token",
        indexes = {
                @Index(name = "idx_token_id", columnList = "id"),
                @Index(name = "idx_user_id", columnList = "user_id", unique = true)
        }
)
public class FcmTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT 설정
    @Column(name = "id")
    private Long id;

    @Column(name = "fcmToken")
    private String fcmToken;

    @Column(name = "appId")
    private String appId;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
    @JoinColumn(name = "userId") // 외래 키 설정
    private UserEntity user;

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = (fcmToken != null) ? fcmToken : this.fcmToken;
    }
}
