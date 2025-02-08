package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "팀 정보 DTO")
public class TeamInfoDto {

    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "팀 경기 결과 (WIN, LOSE)", example = "WIN")
    private MatchResultType result;

    @Schema(description = "팀에 속한 유저 리스트")
    private List<UserSimpleDto> userList;

    public TeamInfoDto(Long id, MatchResultType result, List<UserSimpleDto> userList) {
        this.teamId = id;
        this.result = result;
        this.userList = userList;
    }
}
