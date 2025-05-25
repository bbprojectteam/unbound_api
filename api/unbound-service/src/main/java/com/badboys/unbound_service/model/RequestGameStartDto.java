package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RequestGameStartDto {

    @Schema(description = "a팀 유저 id목록", example = "[1, 2, 3]")
    List<Long> aTeamIdList;

    @Schema(description = "b팀 유저 id목록", example = "[1, 2, 3]")
    List<Long> bTeamIdList;

    @Schema(description = "지역 id", example = "1")
    Long regionId;

    @Schema(description = "매치명", example = "매치 명")
    String matchName;
}
