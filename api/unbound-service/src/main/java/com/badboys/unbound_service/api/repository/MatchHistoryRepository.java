package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.MatchHistoryEntity;
import com.badboys.unbound_service.model.MatchHistoryDto;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchHistoryRepository extends JpaRepository<MatchHistoryEntity, Long> {

    @Query("SELECT DISTINCT mh FROM MatchHistoryEntity mh " +
            "JOIN FETCH mh.teamList t " +  // 팀 정보 가져오기
            "JOIN FETCH t.userList u " +  // 유저 정보 가져오기
            "WHERE u.id = :userId")
    List<MatchHistoryEntity> findByUserId(@Param("userId") Long userId);

    @Query("SELECT mh FROM MatchHistoryEntity mh " +
            "WHERE mh.region.id = :regionId")
    List<MatchHistoryEntity> findByRegionId(@Param("regionId") Long regionId);

}
