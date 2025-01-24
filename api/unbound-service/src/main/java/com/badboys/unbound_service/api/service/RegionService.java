package com.badboys.unbound_service.api.service;

import com.badboys.unbound_service.api.repository.RegionRepository;
import com.badboys.unbound_service.entity.RegionEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RegionService {

    private final RegionRepository regionRepository;

    @Autowired
    public RegionService(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @PostConstruct
    public void preloadCache() {
        getAllRegions(); // 모든 지역 데이터 캐시에 로드
    }

    @Cacheable(value = "regions")
    public List<RegionEntity> getAllRegions() {
        return regionRepository.findAll();
    }

    @Cacheable(value = "regionTree", key = "#parentId")
    public List<RegionEntity> getAllChildren(Long parentId) {
        RegionEntity parent = regionRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 지역을 찾을 수 없습니다."));
        List<RegionEntity> result = new ArrayList<>();
        findAllChildren(parent, result);
        return result;
    }

    private void findAllChildren(RegionEntity parent, List<RegionEntity> result) {
        if (parent != null && parent.getChildren() != null) {
            result.addAll(parent.getChildren());
            for (RegionEntity child : parent.getChildren()) {
                findAllChildren(child, result);
            }
        }
    }
}


