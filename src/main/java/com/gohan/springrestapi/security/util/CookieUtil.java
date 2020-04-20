package com.gohan.springrestapi.security.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    @Value("${api-auth.cookie.access-token-cookie-name}")
    private String accessTokenCookieName;

    @Value("${api-auth.cookie.refresh-token-cookie-name}")
    private String refreshTokenCookieName;

    @Value("${api-auth.cookie.secure}")
    private boolean secure;

    @Value("${api-auth.cookie.domain:#{null}}")
    private String domain;

    public HttpCookie createAccessTokenCookie(String token, Long duration) {
        return ResponseCookie.from(accessTokenCookieName, token)
                .maxAge(duration)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict") // protect against csrf attacks on modern browser
                .domain(domain)
                .path("/")
                .build();
    }

    public HttpCookie createRefreshTokenCookie(String token, Long duration) {
        return ResponseCookie.from(refreshTokenCookieName, token)
                .maxAge(duration)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .domain(domain)
                .path("/")
                .build();
    }
}
