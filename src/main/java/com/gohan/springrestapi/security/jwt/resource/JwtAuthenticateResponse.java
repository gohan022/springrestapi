package com.gohan.springrestapi.security.jwt.resource;

import lombok.Data;
import lombok.NonNull;

public class JwtAuthenticateResponse {
    private String token;

    public JwtAuthenticateResponse(String token) {
        this.token = "Bearer " + token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "JwtLoginResponse{" +
                ", token='" + token + '\'' +
                '}';
    }
}
