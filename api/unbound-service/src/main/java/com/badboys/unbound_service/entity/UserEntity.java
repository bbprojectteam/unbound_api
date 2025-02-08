package com.badboys.unbound_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
    @JoinColumn(name = "regionId") // 외래 키 설정
    private RegionEntity region;

    @ManyToMany(mappedBy = "userList")
    private List<TeamEntity> teamList;

    @ManyToMany(mappedBy = "userList")
    private List<ChatRoomEntity> chatRoomList;


    public void updateUser(String username, String birth, String gender, RegionEntity region) {
        this.username = (username != null) ? username : this.username;
        this.birth = (birth != null) ? birth : this.birth;
        this.gender = (gender != null) ? gender : this.gender;
        this.region = (region != null) ? region : this.region;
    }

    public void updateMmr(int newMmr) {
        this.mmr = newMmr;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
