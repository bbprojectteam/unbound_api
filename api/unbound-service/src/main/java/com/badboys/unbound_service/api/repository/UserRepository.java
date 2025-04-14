package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT COUNT(m.id) " +
            "FROM MatchInfoEntity m " +
            "JOIN m.teamList t " +
            "JOIN t.userList u " +
            "WHERE u.id = :userId AND m.endAt IS NOT NULL")
    int countUserMatchHistory(@Param("userId") Long userId);

}
