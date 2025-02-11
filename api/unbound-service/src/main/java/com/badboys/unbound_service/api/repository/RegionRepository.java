package com.badboys.unbound_service.api.repository;

import com.badboys.unbound_service.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<RegionEntity, Long> {

    // 특정 부모 ID로 자식 데이터 가져오기
    List<RegionEntity> findByParentId(Long parentId);

    // 최상위 지역 가져오기 (부모가 없는 지역)
    List<RegionEntity> findByParentIsNull();
}