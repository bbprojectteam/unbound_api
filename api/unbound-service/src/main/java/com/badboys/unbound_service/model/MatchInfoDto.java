package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "매치 기록 DTO")
public class MatchInfoDto {

    @Schema(description = "매치 ID", example = "101")
    private Long matchInfoId;

    @Schema(description = "매치 명", example = "매치 명")
    private String matchName;

    @Schema(description = "매치 시작 시간", example = "2025-02-08T15:00:00")
    private LocalDateTime startAt;

    @Schema(description = "매치 종료 시간", example = "2025-02-08T16:00:00")
    private LocalDateTime endAt;

    @Schema(description = "지역 ID", example = "1")
    private Long regionId;

    @Schema(description = "위도", example = "111.00")
    private Double latitude;

    @Schema(description = "경도", example = "111.00")
    private Double longitude;

    @Schema(description = "참여한 팀 리스트", example = "[{teamId: 1, result: 'WIN', userList: [...]}, {teamId: 2, result: 'LOSE', userList: [...]}]")
    private List<TeamInfoDto> teamList;
}
