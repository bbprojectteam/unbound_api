package com.badboys.unbound_service.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

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

    @Column(name = "location")
    private String location;

    @Column(name = "description")
    private String description;

    @Column(name = "matchDt")
    private String matchDt;

    @Column(name = "threeOnThreeYn", columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String threeOnThreeYn;

    @Column(name = "ballYn", columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String ballYn;

    @Column(name = "refereeYn", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String refereeYn;

    @Column(name = "backBoardYn", columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String backBoardYn;

    @Column(name = "threePointLimitYn", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String threePointLimitYn;

    @Column(name = "halfCourtYn", columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String halfCourtYn;

    private Double latitude;

    private Double longitude;

    @BatchSize(size = 10)
    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMemberEntity> chatMemberList = new ArrayList<>();

    public void updateChatRoomInfo(String name, String location, String description, String matchDt, String threeOnThreeYn,
                                   String ballYn, String refereeYn, String backBoardYn, String threePointLimitYn, String halfCourtYn, Double latitude, Double longitude) {
        this.name = (name != null) ? name : this.name;
        this.location = (location != null) ? location : this.location;
        this.description = (description != null) ? description : this.description;
        this.matchDt = (matchDt != null) ? matchDt : this.matchDt;
        this.threeOnThreeYn = (threeOnThreeYn != null) ? threeOnThreeYn : this.threeOnThreeYn;
        this.ballYn = (ballYn != null) ? ballYn : this.ballYn;
        this.refereeYn = (refereeYn != null) ? refereeYn : this.refereeYn;
        this.backBoardYn = (backBoardYn != null) ? backBoardYn : this.backBoardYn;
        this.threePointLimitYn = (threePointLimitYn != null) ? threePointLimitYn : this.threePointLimitYn;
        this.halfCourtYn = (halfCourtYn != null) ? halfCourtYn : this.halfCourtYn;
        this.latitude = (latitude != null) ? latitude : this.latitude;
        this.longitude = (longitude != null) ? longitude : this.longitude;
    }

    public void addChatMember(ChatMemberEntity member) {
        chatMemberList.add(member);
        member.setChatRoom(this);
    }

    public void removeChatMember(ChatMemberEntity member) {
        chatMemberList.remove(member);
        member.setChatRoom(null);
    }
}
