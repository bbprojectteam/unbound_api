package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "간단한 유저 정보 DTO")
public class UserSimpleDto {

    @Schema(description = "유저 닉네임", example = "Alice")
    private String username;

    @Schema(description = "유저 MMR 점수", example = "1500")
    private int mmr;

    public UserSimpleDto(String username, int mmr) {
        this.username = username;
        this.mmr = mmr;
    }
}
