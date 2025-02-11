package com.badboys.unbound_auth.api.repository;

import com.badboys.unbound_auth.api.entity.FcmTokenEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmTokenEntity, Long> {

    // 특정 유저 & 특정 앱(`appId`)의 FCM 토큰 조회
    Optional<FcmTokenEntity> findByUserIdAndAppId(Long userId, String appId);
}
