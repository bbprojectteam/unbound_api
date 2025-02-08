package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Schema(description = "유저 정보 DTO")
public class UserInfoDto {

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "유저명", example = "wukim")
    private String username;

    @Schema(description = "성별 (M/F)", example = "M")
    private String gender;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImage;

    @Schema(description = "유저 MMR 점수", example = "1000")
    private Long mmr;

    @Schema(description = "지역명", example = "대전광역시 서구 괴정동")
    private String regionNm;

    @Schema(description = "지역 ID", example = "1")
    private Long regionId;
}
