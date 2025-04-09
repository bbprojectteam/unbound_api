package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "게임 참가 팀 아이디")
public class ResponseGameStartDto {

    @Schema(description = "매치 ID", example = "101")
    private Long matchInfoId;

    @Schema(description = "a팀아이디", example = "1")
    private Long aTeamId;

    @Schema(description = "b팀아이디", example = "2")
    private Long bTeamId;
}
