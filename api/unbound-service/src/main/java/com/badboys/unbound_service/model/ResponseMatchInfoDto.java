package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "매치 상세화면 정보 DTO")
public class ResponseMatchInfoDto {

    private MatchHistoryDto matchInfo;

    private List<CommentDto> commentList;
}
