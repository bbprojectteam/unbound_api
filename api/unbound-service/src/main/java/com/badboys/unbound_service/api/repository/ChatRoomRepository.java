package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.ChatRoomEntity;
import com.badboys.unbound_service.model.ChatRoomThumbnailDto;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    @Query("""
        SELECT new com.badboys.unbound_service.model.ChatRoomThumbnailDto(cr.id, cr.name, SIZE(cr.chatMemberList), cr.regionId)
        FROM ChatRoomEntity cr
        WHERE NOT EXISTS (
            SELECT 1
            FROM ChatMemberEntity cm
            WHERE cm.chatRoom.id = cr.id
              AND cm.user.id = :userId
        )
        AND cr.regionId IN :regionIdList
        GROUP BY cr.id
    """)
    List<ChatRoomThumbnailDto> findUnjoinedChatRoomList(
            @Param("userId") Long userId,
            @Param("regionIdList") List<Long> regionIdList
    );

}
