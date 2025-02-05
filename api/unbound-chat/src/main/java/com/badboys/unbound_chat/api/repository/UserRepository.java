package com.badboys.unbound_chat.api.repository;

import com.badboys.unbound_chat.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

}
