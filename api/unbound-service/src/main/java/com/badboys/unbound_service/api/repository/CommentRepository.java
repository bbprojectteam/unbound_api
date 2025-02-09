package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
}
