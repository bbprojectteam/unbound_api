package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RequestMatchDto {

    private Long userId;

    private int mmr;

    private List<Long> regionRange;

    private Long regionId;
}
