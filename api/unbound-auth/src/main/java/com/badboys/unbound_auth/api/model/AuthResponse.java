package com.badboys.unbound_auth.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private String username;

    private String accessToken;

    private String refreshToken;
}
