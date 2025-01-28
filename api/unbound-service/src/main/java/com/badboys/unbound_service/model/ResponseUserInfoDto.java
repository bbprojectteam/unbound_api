package com.badboys.unbound_service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseUserInfoDto {

    private String username;

    private char gender;

    private String profileImage;

    private Long mmr;

    private String regionNm;
}
