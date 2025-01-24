package com.badboys.unbound_service.model;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfo {

    private String username;

    private char gender;

    @Column(name = "profileImage")
    private String profileImage;

    @Column(name = "mmr")
    private Long mmr;


}
