package com.badboys.unbound_chat.api.repository;

import com.badboys.unbound_chat.api.entity.FcmTokenEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmTokenEntity, Long> {

    @Query("SELECT f.token FROM FcmTokenEntity f WHERE f.user.id IN :userIds")
    Set<String> findTokensByUserIds(@Param("userIds") Set<Long> userIds);

    // 특정 FCM 토큰 삭제
    @Modifying
    @Query("DELETE FROM FcmTokenEntity f WHERE f.token = :fcmToken")
    void deleteByToken(@Param("fcmToken") String fcmToken);
}
