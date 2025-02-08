package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class RequestMatchDto {

    private String userId;

    private int mmr;

    private List<Long> regionIdList;

    public RequestMatchDto(String userId, int mmr, List<Long> regionIdList) {
        this.userId = userId;
        this.mmr = mmr;
        this.regionIdList = regionIdList;
    }
}
