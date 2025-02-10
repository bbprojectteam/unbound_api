package com.badboys.unbound_service.entity;

import com.badboys.unbound_service.model.MatchResultType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "team",
        indexes = {
                @Index(name = "idx_team_id", columnList = "id")
        }
)
public class TeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String score;

    private MatchResultType result;

    @ManyToOne
    @JoinColumn(name = "matchInfoId")
    private MatchInfoEntity matchInfo;

    @ManyToMany
    @JoinTable(
            name = "team_user",
            joinColumns = @JoinColumn(name = "teamId"),
            inverseJoinColumns = @JoinColumn(name = "userId")
    )
    @Builder.Default
    private Set<UserEntity> userList = new HashSet<>();
}
