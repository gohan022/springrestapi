package com.gohan.springrestapi.security;

import org.springframework.security.core.AuthenticationException;

public class TokenAuthenticationException extends AuthenticationException {
    public TokenAuthenticationException(String msg, Throwable t) {
        super(msg, t);
    }

    public TokenAuthenticationException(String msg) {
        super(msg);
    }
}
