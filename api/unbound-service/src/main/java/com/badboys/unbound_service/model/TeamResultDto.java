package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamResultDto {

    @Schema(description = "팀 아이디", example = "1")
    private Long teamId;

    @Schema(description = "점수", example = "2")
    private int score;
}
