package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestUpdateChatRoomDto {

    @Schema(description = "방제목", example = "익산농구")
    private String name;

    @Schema(description = "경기 날짜", example = "2025-03-26 08:00")
    private String matchDt;

    @Schema(description = "장소", example = "원고 운동장")
    private String location;

    @Schema(description = "메모, 추가정보, 기타사항", example = "늦으면 벌금")
    private String description;

    @Schema(description = "3대3 여부", example = "Y")
    private String threeOnThreeYn;

    @Schema(description = "농구공 유무", example = "Y")
    private String ballYn;

    @Schema(description = "심판 유무", example = "Y")
    private String refereeYn;

    @Schema(description = "백보드 유무", example = "Y")
    private String backBoardYn;

    @Schema(description = "3점 제한 여부", example = "Y")
    private String threePointLimitYn;

    @Schema(description = "반코트 여부", example = "Y")
    private String halfCourtYn;

    @Schema(description = "위도", example = "111.00")
    private Double latitude;

    @Schema(description = "경도", example = "111.00")
    private Double longitude;
}
