package com.badboys.unbound_chat.api.entity;

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

    @Column(name = "refreeYn", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String refreeYn;

    @Column(name = "backBoardYn", columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String backBoardYn;

    @Column(name = "threePointLimitYn", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String threePointLimitYn;

    @Column(name = "halfCourtYn", columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String halfCourtYn;

    @BatchSize(size = 10)
    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMemberEntity> chatMemberList = new ArrayList<>();
}
