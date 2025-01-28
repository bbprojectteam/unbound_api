package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

}
