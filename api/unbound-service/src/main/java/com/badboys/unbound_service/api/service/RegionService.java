package com.badboys.unbound_service.api.service;

import com.badboys.unbound_service.api.repository.RegionRepository;
import com.badboys.unbound_service.entity.RegionEntity;
import com.badboys.unbound_service.model.Region;
import com.badboys.unbound_service.model.ResponseUserInfoDto;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RegionService {

    private final RegionRepository regionRepository;
    
    private final ModelMapper modelMapper;

    @Autowired
    public RegionService(RegionRepository regionRepository, ModelMapper modelMapper) {
        this.regionRepository = regionRepository;
        this.modelMapper = modelMapper;
    }

    @PostConstruct
    public void preloadCache() {
        getAllRegions(); // 모든 지역 데이터 캐시에 로드
    }

    @Cacheable(value = "regions")
    public List<RegionEntity> getAllRegions() {
        List<RegionEntity> regions = regionRepository.findAll();
        return regions;
    }

    @Cacheable(value = "regionTree", key = "#parentId")
    public List<Long> getAllChildrenId(Long parentId) {
        RegionEntity parent = regionRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 지역을 찾을 수 없습니다."));

        List<Long> childrenIdList = new ArrayList<>();
        childrenIdList.add(parent.getId());
        findAllChildren(parent, childrenIdList);
        return childrenIdList;
    }

    private void findAllChildren(RegionEntity parent, List<Long> childrenIdList) {
        if (parent == null || parent.getChildren() == null) return;

        for (RegionEntity child : parent.getChildren()) {
            childrenIdList.add(child.getId());
            findAllChildren(child, childrenIdList);
        }
    }

    @Cacheable(value = "regionParentTree", key = "#childId")
    public List<RegionEntity> getAllParentsWithCache(Long childId) {
        return getAllParents(childId); // 위에서 작성한 메서드를 호출
    }

    public List<RegionEntity> getAllParents(Long childId) {
        RegionEntity child = regionRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 지역을 찾을 수 없습니다."));

        List<RegionEntity> parents = new ArrayList<>();
        findAllParents(child, parents);
        return parents;
    }

    private void findAllParents(RegionEntity child, List<RegionEntity> parents) {
        if (child.getParent() != null) {
            parents.add(child.getParent());
            findAllParents(child.getParent(), parents);
        }
    }


}


