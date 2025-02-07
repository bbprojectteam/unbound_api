package com.badboys.unbound_service.api.service;

import com.badboys.unbound_service.api.repository.RegionRepository;
import com.badboys.unbound_service.entity.RegionEntity;
import com.badboys.unbound_service.model.Region;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RegionService {

    private final RegionRepository regionRepository;
    private final ApplicationContext applicationContext;
    private final ModelMapper modelMapper;

    @Autowired
    public RegionService(RegionRepository regionRepository, ApplicationContext applicationContext, ModelMapper modelMapper) {
        this.regionRepository = regionRepository;
        this.applicationContext = applicationContext;
        this.modelMapper = modelMapper;
    }

    @Cacheable(value = "regions")
    public List<Region> getAllRegions() {
        List<RegionEntity> regions = regionRepository.findAll();
        List<Region> allRegionList = regions.stream()
                .map(entity -> {
                    Region region = modelMapper.map(entity, Region.class);
                    region.setParentId(entity.getParent() != null ? entity.getParent().getId() : null);
                    return region;
                })
                .collect(Collectors.toList());
        return allRegionList;
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
        if (parent == null || parent.getChildList() == null) return;

        for (RegionEntity child : parent.getChildList()) {
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

    public RegionEntity getRegion(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("Region not found with ID: " + regionId));
    }


}


