package com.badboys.unbound_auth.api.repository;

import com.badboys.unbound_auth.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByUid(String uid);

    boolean existsByUid(String uid);
}