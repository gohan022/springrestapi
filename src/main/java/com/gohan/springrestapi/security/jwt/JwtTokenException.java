package com.gohan.springrestapi.security.jwt;

import io.jsonwebtoken.JwtException;

public class JwtTokenException extends JwtException {
    public JwtTokenException(String message) {
        super(message);
    }

    public JwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
