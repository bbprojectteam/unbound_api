package com.badboys.unbound_auth.filter;

import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {

    private final String uid;
    private final FirebaseToken firebaseToken;

    public FirebaseAuthenticationToken(String uid, FirebaseToken firebaseToken) {
        super(null);
        this.uid = uid;
        this.firebaseToken = firebaseToken;
        setAuthenticated(true); // 인증 성공 상태로 설정
    }

    @Override
    public Object getCredentials() {
        return null; // Credentials는 사용하지 않음
    }

    @Override
    public Object getPrincipal() {
        return this.uid; // 인증된 사용자의 UID 반환
    }

    public FirebaseToken getFirebaseToken() {
        return firebaseToken; // Firebase Token 정보 반환
    }
}
