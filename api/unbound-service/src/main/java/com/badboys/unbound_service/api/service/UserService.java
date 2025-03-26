package com.badboys.unbound_service.api.service;

import com.badboys.unbound_service.api.repository.UserRepository;
import com.badboys.unbound_service.entity.RegionEntity;
import com.badboys.unbound_service.entity.UserEntity;
import com.badboys.unbound_service.model.RequestUpdateUserDto;
import com.badboys.unbound_service.model.UserInfoDto;
import com.badboys.unbound_service.model.UserSimpleDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    private final RegionService regionService;

    private final S3Service s3Service;

    @Autowired
    public UserService(UserRepository userRepository, ModelMapper modelMapper, RegionService regionService, S3Service s3Service) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.regionService = regionService;
        this.s3Service = s3Service;
    }

    public UserInfoDto getUserInfo(Long userId) {
        UserEntity userEntity = getUserEntity(userId);
        UserInfoDto userInfoDto = modelMapper.map(userEntity, UserInfoDto.class);
        RegionEntity regionEntity = userEntity.getRegion();
        Long regionId = regionEntity.getId();
        List<RegionEntity> regionEntityList = regionService.getAllParents(regionId);
        StringBuffer regionNm = new StringBuffer();
        for (RegionEntity region : regionEntityList) {
            regionNm.insert(0, region.getName());
        }
        userInfoDto.setUserId(userId);
        userInfoDto.setRegionNm(regionNm.toString());
        userInfoDto.setRegionId(regionId);
        return userInfoDto;
    }

    public UserEntity getUserEntity(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저가 존재하지 않습니다."));
        return userEntity;
    }

    public void updateUser(Long userId, RequestUpdateUserDto requestUpdateUserDto) throws IllegalArgumentException{

        UserEntity userEntity = getUserEntity(userId);
        RegionEntity regionEntity = null;
        if (requestUpdateUserDto.getRegionId() != null) {
            regionEntity = regionService.getRegion(requestUpdateUserDto.getRegionId());
        }

        userEntity.updateUser(
                requestUpdateUserDto.getUsername(),
                requestUpdateUserDto.getBirth(),
                requestUpdateUserDto.getGender(),
                regionEntity
        );
        userRepository.save(userEntity);
    }

    public void updateUserProfileImage(Long userId, MultipartFile profileImageFile) {
        UserEntity userEntity = getUserEntity(userId);
        String profileImageUrl = null;

        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            try {
                profileImageUrl = s3Service.uploadFile(profileImageFile);
            } catch (RuntimeException e) {
                throw new RuntimeException("프로필 이미지 업로드 중 오류 발생", e);
            }
        }

        userEntity.updateProfileImage(profileImageUrl);
        userRepository.save(userEntity);
    }

    public List<UserSimpleDto> convertToUserSimpleDto(Set<UserEntity> userEntities) {
        return userEntities.stream()
                .map(user -> new UserSimpleDto(user.getId(), user.getUsername(), user.getProfileImage(), user.getMmr(), user.getIntroduction()))
                .collect(Collectors.toList());
    }
}
