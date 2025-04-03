package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
}
