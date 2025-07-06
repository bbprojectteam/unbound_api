package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT COUNT(m.id) " +
            "FROM MatchInfoEntity m " +
            "JOIN m.teamList t " +
            "JOIN t.userList u " +
            "WHERE u.id = :userId AND m.endAt IS NOT NULL")
    int countUserMatchHistory(@Param("userId") Long userId);

    @Query("""
        SELECT u FROM UserEntity u 
        WHERE u.region.id IN :regionIds 
          AND u.id NOT IN (
              SELECT cm.user.id 
              FROM ChatMemberEntity cm 
              WHERE cm.chatRoom.id = :chatRoomId
          )
    """)
    Set<UserEntity> findInvitableUsers(@Param("regionIds") List<Long> regionIds,
                                      @Param("chatRoomId") Long chatRoomId);

}
