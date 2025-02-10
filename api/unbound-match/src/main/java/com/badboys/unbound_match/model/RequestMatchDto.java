package com.badboys.unbound_match.model;

import lombok.Data;
import java.util.List;

@Data
public class RequestMatchDto {

    private Long userId;

    private int mmr;

    private List<Long> regionRange;

    private Long regionId;
}
