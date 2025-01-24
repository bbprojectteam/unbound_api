package com.badboys.unbound_auth.api.repository;

import com.badboys.unbound_auth.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByUid(String uid);

    boolean existsByUid(String uid);
}