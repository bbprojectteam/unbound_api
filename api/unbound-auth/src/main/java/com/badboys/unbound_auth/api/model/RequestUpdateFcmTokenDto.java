package com.badboys.unbound_auth.api.model;

import lombok.Data;

@Data
public class RequestUpdateFcmTokenDto {

    private String appId;

    private String fcmToken;
}
