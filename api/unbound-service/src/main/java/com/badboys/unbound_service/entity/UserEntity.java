package com.badboys.unbound_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "user",
        indexes = {
                @Index(name = "idx_user_id", columnList = "id"),
                @Index(name = "idx_user_uid", columnList = "uid", unique = true)
        }
)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT 설정
    @Column(name = "id")
    private Long id;

    @Column(name = "uid", unique = true ,nullable = false) // 중복 방지 설정
    private String uid;

    @Column(name = "username")
    private String username;

    @Column(name = "birth")
    private String birth;

    @Column(name = "gender")
    private char gender;

    @Column(name = "profileImage")
    private String profileImage;

    @Column(name = "mmr")
    private Long mmr;
}
