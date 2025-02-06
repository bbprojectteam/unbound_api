package com.badboys.unbound_gateway.api;

import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/.well-known/acme-challenge")
public class LetsEncryptController {

    private final Map<String, String> challenges = new ConcurrentHashMap<>();

    @GetMapping("/{token}")
    public String getChallenge(@PathVariable String token) {
	String response = challenges.getOrDefault(token, ""); // 기존: 빈 문자열 반환 가능성 있음
        System.out.println("Returning challenge for token: " + token + " -> " + response);
        return response; // 올바른 토큰이 반환되는지 확인
    }

    @PostMapping("/{token}")
    public void setChallenge(@PathVariable String token, @RequestBody String keyAuth) {
        challenges.put(token, keyAuth);
	System.out.println("Stored challenge: " + token + " -> " + keyAuth);
    }
}
