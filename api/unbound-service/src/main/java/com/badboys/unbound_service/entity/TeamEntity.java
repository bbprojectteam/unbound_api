package com.badboys.unbound_service.entity;

import com.badboys.unbound_service.model.MatchResultType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.List;

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
    @JoinColumn(name = "matchHistoryId")
    private MatchHistoryEntity matchHistory;

    @BatchSize(size = 3)
    @ManyToMany
    @JoinTable(
            name = "team_user",
            joinColumns = @JoinColumn(name = "teamId"),
            inverseJoinColumns = @JoinColumn(name = "userId")
    )
    private List<UserEntity> userList;
}
