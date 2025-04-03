package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestGameEndDto {

    @Schema(description = "매치 정보 id")
    private Long matchInfoId;

    @Schema(description = "a팀 결과")
    private TeamResultDto aTeamResult;

    @Schema(description = "b팀 결과")
    private TeamResultDto bTeamResult;

    @Schema(description = "승리팀 아이디")
    private Long winnerTeamId;
}
