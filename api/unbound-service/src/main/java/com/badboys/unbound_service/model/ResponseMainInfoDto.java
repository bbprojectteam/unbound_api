package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "메인 화면 정보 DTO")
public class ResponseMainInfoDto {

    @Schema(description = "유저 정보", example = "{userId: 1, username: 'wukim', regionNm: '대전광역시 서구 괴정동'}")
    private UserInfoDto userInfo;

    @Schema(description = "유저가 참여한 매치 기록 목록")
    private List<MatchHistoryDto> userMatchHistoryList;

    @Schema(description = "같은 지역의 매치 기록 목록")
    private List<MatchHistoryDto> regionMatchHistoryList;
}
