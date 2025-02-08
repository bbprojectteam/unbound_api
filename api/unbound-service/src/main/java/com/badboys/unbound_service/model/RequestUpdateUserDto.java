package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.*;


@Data
@Schema(description = "유저 정보 업데이트 요청 DTO")
public class RequestUpdateUserDto {

    @Schema(description = "유저명", example = "wukim")
    private String username;

    @Schema(description = "생년월일 (YYYY-MM-DD)", example = "1995-08-15")
    private String birth;

    @Schema(description = "성별 (M: 남성, F: 여성)", example = "M")
    private String gender;

    @Schema(description = "지역 ID", example = "1")
    private Long regionId;
}
