package com.badboys.unbound_auth.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
}
