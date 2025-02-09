package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RequestMatchDto {

    private String userId;

    private int mmr;

    private List<Long> regionIdList;
}
