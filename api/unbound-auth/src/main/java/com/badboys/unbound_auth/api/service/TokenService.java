package com.badboys.unbound_auth.api.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

public interface TokenService {

    String generateToken(Long userId, String type);
    void saveRefreshToken(Long userId, String refreshToken);
    Long getUserId(String refreshToken);
    void deleteRefreshToken(String refreshToken);
    FirebaseToken verifyTokenHeader(String authorizationHeader) throws FirebaseAuthException;
}
