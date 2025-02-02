package com.badboys.unbound_service.api.service;

import com.badboys.unbound_service.api.repository.UserRepository;
import com.badboys.unbound_service.entity.RegionEntity;
import com.badboys.unbound_service.entity.UserEntity;
import com.badboys.unbound_service.model.ResponseUserInfoDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    private final RegionService regionService;

    @Autowired
    public UserService(UserRepository userRepository, ModelMapper modelMapper, RegionService regionService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.regionService = regionService;
    }

    public ResponseUserInfoDto getUserInfo(Long userId) {
        UserEntity userEntity = getUserEntity(userId);
        ResponseUserInfoDto responseUserInfoDto = modelMapper.map(userEntity, ResponseUserInfoDto.class);
        RegionEntity regionEntity = userEntity.getRegion();
        Long regionId = regionEntity.getId();
        List<RegionEntity> regionEntityList = regionService.getAllParents(regionId);
        StringBuffer regionNm = new StringBuffer();
        for (RegionEntity region : regionEntityList) {
            regionNm.insert(0, region.getName());
        }
        responseUserInfoDto.setRegionNm(regionNm.toString());
        return responseUserInfoDto;
    }

    public UserEntity getUserEntity(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT, "User not found"));
        return userEntity;
    }

}
