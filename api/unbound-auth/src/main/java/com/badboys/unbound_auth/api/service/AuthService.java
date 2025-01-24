package com.badboys.unbound_auth.api.service;

import com.badboys.unbound_auth.api.entity.UserEntity;
import org.springframework.http.HttpHeaders;

public interface AuthService {

    Long saveUser(UserEntity user);
    HttpHeaders registerUser(String authorizationHeader) throws Exception;
    HttpHeaders login(String authorizationHeader) throws Exception;
}
