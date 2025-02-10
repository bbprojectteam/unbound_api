package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "지역 정보 DTO")
public class Region implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "지역 ID", example = "1")
    private Long id;

    @Schema(description = "지역명", example = "서울")
    private String name;

    @Schema(description = "지역 타입", example = "특별시")
    private RegionType type;

    @Schema(description = "지역 레벨 (상위/하위 지역 구분)", example = "2")
    private int depth;

    @Schema(description = "상위 지역 ID (없으면 null)", example = "10")
    private Long parentId;
}
