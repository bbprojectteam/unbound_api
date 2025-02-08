package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.MatchHistoryEntity;
import com.badboys.unbound_service.model.MatchHistoryDto;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchHistoryRepository extends JpaRepository<MatchHistoryEntity, Long> {

    @Query("SELECT mh FROM MatchHistoryEntity mh " +
            "JOIN mh.teamList t " +
            "JOIN t.userList u " +
            "WHERE u.id = :userId")
    List<MatchHistoryDto> findByUserId(@Param("userId") Long userId);

    @Query("SELECT mh FROM MatchHistoryEntity mh " +
            "WHERE mh.region.id = :regionId")
    List<MatchHistoryDto> findByRegionId(@Param("regionId") Long regionId);
}
