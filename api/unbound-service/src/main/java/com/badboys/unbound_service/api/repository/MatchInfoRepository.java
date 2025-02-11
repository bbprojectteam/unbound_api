package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.MatchInfoEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchInfoRepository extends JpaRepository<MatchInfoEntity, Long> {

    @EntityGraph(attributePaths = {"teamList", "teamList.userList"})
    @Query("SELECT m FROM MatchInfoEntity m " +
            "WHERE EXISTS (SELECT 1 FROM TeamEntity t JOIN t.userList u WHERE t.matchInfo = m AND u.id = :userId) " +
            "ORDER BY m.id DESC")
    Page<MatchInfoEntity> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT m FROM MatchInfoEntity m " +
            "WHERE m.region.id = :regionId " +
            "ORDER BY m.id DESC")
    Page<MatchInfoEntity> findByRegionId(@Param("regionId") Long regionId, Pageable pageable);

}
