package com.badboys.unbound_service.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto {

    private Long userId;

    private String username;

    private String gender;

    private String profileImage;

    private Long mmr;

    private String regionNm;

    private Long regionId;
}
