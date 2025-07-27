package com.badboys.unbound_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private String gender;

    @Column(name = "profileImage")
    private String profileImage;

    @Column(name = "mmr")
    private int mmr;

    @Column(name = "introduction", length = 1000)
    private String introduction;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
    @JoinColumn(name = "regionId") // 외래 키 설정
    private RegionEntity region;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommentEntity> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TeamUserEntity> teamUsers = new HashSet<>();

    @BatchSize(size = 10)
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMemberEntity> chatMemberList = new ArrayList<>();

    public void updateUser(String username, String birth, String gender, String introduction, RegionEntity region) {
        this.username = (username != null) ? username : this.username;
        this.birth = (birth != null) ? birth : this.birth;
        this.gender = (gender != null) ? gender : this.gender;
        this.introduction = (introduction != null) ? introduction : this.gender;
        this.region = (region != null) ? region : this.region;
    }

    public void updateMmr(int newMmr) {
        this.mmr = newMmr;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = (profileImage != null) ? profileImage : this.profileImage;
    }
}
