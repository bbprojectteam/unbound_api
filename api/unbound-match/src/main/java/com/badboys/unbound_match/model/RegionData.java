package com.badboys.unbound_match.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RegionData {
    private List<Long> regionRange;
    private Long regionId;
}
