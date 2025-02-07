package com.badboys.unbound_service.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseUserInfoDto {

    private String username;

    private char gender;

    private String profileImage;

    private Long mmr;

    private String regionNm;
}
