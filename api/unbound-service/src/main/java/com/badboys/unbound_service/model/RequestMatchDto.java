package com.badboys.unbound_service.model;

import lombok.Data;

import java.util.List;

@Data
public class RequestMatchDto {

    private Long userId;

    private int mmr;

    private List<Long> regionIdList;

    public RequestMatchDto(Long userId, int mmr, List<Long> regionIdList) {
        this.userId = userId;
        this.mmr = mmr;
        this.regionIdList = regionIdList;
    }
}
