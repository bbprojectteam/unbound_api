package com.badboys.unbound_service.model;

import lombok.Data;

import java.util.List;

@Data
public class ResponseMainInfoDto {

    private UserInfoDto userInfo;

    private List<MatchHistoryDto> userMatchHistoryList;

    private List<MatchHistoryDto> regionMatchHistoryList;
}
