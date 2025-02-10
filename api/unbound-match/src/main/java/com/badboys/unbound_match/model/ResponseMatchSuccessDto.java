package com.badboys.unbound_match.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class ResponseMatchSuccessDto {

    private Set<Long> userIdSet;

    private Set<Long> regionIdSet;
}
