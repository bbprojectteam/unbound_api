package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "간단한 유저 정보 DTO")
public class UserSimpleDto {

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "유저명", example = "Alice")
    private String username;

    @Schema(description = "유저 프사", example = "")
    private String profileImage;

    @Schema(description = "유저 MMR 점수", example = "1500")
    private int mmr;

    @Schema(description = "자기소개", example = "익산사는 Alice입니다.")
    private String introduction;

    public UserSimpleDto(Long userId, String username, String profileImage, int mmr, String introduction) {
        this.userId = userId;
        this.username = username;
        this.profileImage = profileImage;
        this.mmr = mmr;
        this.introduction = introduction;
    }
}
