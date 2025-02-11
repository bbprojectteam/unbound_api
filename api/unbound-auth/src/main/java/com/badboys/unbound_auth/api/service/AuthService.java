package com.badboys.unbound_auth.api.service;

import com.badboys.unbound_auth.api.entity.UserEntity;
import com.badboys.unbound_auth.api.model.RequestUpdateFcmTokenDto;
import org.springframework.http.HttpHeaders;

public interface AuthService {

    Long saveUser(UserEntity user);
    HttpHeaders registerUser(String authorizationHeader) throws Exception;
    HttpHeaders login(String authorizationHeader, RequestUpdateFcmTokenDto requestUpdateFcmTokenDto) throws Exception;
}
