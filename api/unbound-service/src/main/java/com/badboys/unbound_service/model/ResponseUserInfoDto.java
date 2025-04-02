package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "내 정보 DTO")
public class ResponseUserInfoDto {

    @Schema(description = "유저 정보", example = "{userId: 1, username: 'wukim', regionNm: '대전광역시 서구 괴정동'}")
    private UserInfoDto userInfo;

    @Schema(description = "유저가 참여한 매치 기록 목록")
    private List<MatchHistoryDto> userMatchHistoryList;
}
