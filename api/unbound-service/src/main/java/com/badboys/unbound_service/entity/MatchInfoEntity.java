package com.badboys.unbound_service.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
        name = "match_info",
        indexes = {
                @Index(name = "idx_info_id", columnList = "id")
        }
)
public class MatchInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matchName;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Double latitude;

    private Double longitude;

    @OneToMany(mappedBy = "matchInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TeamEntity> teamList = new HashSet<>();

    @OneToMany(mappedBy = "matchInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommentEntity> commentList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
    @JoinColumn(name = "regionId") // 외래 키 설정
    private RegionEntity region;

    public void updateEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }
}
