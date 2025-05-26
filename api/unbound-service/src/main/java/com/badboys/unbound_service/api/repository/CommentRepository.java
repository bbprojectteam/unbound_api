package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.CommentEntity;
import com.badboys.unbound_service.model.CommentDto;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @Query("SELECT new com.badboys.unbound_service.model.CommentDto(" +
            "c.id, c.content, c.depth, " +
            "u.id, u.username, u.profileImage, " +
            "c.useYn, " +
            "c.updatedAt, " +
            "c.parent.id) " +
            "FROM CommentEntity c " +
            "JOIN c.user u " +
            "WHERE c.matchInfo.id = :matchInfoId")
    List<CommentDto> findAllByMatchInfoWithUser(@Param("matchInfoId") Long matchInfoId);
}
