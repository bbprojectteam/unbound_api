package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RequestMatchStartDto {

    @Schema(description = "제한 지역 아이디", example = "1")
    private Long limitRegionId;
}
