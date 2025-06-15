package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomInfo {

    private Long id;

    @Schema(description = "방제목", example = "익산농구")
    private String name;

    @Schema(description = "지역아이디(매칭에 사용된 고정값)", example = "1")
    private Long regionId;

    @Schema(description = "경기 날짜", example = "2025-03-26 08:00")
    private String matchDt;

    @Schema(description = "장소", example = "원고 운동장")
    private String location;

    @Schema(description = "메모, 추가정보, 기타사항", example = "늦으면 벌금")
    private String description;

    @Schema(description = "3대3여부", example = "CHAR(1) DEFAULT 'Y'")
    private String threeOnThreeYn;

    @Schema(description = "공보유여부", example = "Y")
    private String ballYn;

    @Schema(description = "심판여부", example = "Y")
    private String refereeYn;

    @Schema(description = "골대여부", example = "Y")
    private String backBoardYn;

    @Schema(description = "3점제한여부", example = "Y")
    private String threePointLimitYn;

    @Schema(description = "반코트여부", example = "Y")
    private String halfCourtYn;

    @Schema(description = "위도", example = "127.00")
    private Double latitude;

    @Schema(description = "경도", example = "127.00")
    private Double longitude;

    @Schema(description = "라커룸 참여 여부", example = "Y")
    private String joinedYn;
}
