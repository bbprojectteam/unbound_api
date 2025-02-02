package com.badboys.unbound_match.model;

import lombok.Data;
import java.util.List;

@Data
public class RequestMatchDto {

    private String userId;

    private int mmr;

    private List<Long> regionIdList;
}
