package com.badboys.unbound_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "match_history",
        indexes = {
                @Index(name = "idx_history_id", columnList = "id")
        }
)
public class MatchInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @OneToMany(mappedBy = "matchInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeamEntity> teamList = new ArrayList<>();

    @OneToMany(mappedBy = "matchInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommentEntity> commentList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
    @JoinColumn(name = "regionId") // 외래 키 설정
    private RegionEntity region;
}
