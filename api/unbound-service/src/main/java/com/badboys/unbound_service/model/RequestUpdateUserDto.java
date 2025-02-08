package com.badboys.unbound_service.model;

import jakarta.persistence.Column;
import lombok.*;

@Data
public class RequestUpdateUserDto {

    private String username;

    private String birth;

    private String gender;

    private Long regionId;
}
