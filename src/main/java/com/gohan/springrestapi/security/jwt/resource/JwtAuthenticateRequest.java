package com.gohan.springrestapi.security.jwt.resource;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class JwtAuthenticateRequest {
    @NotEmpty(message = "Please enter a username.")
    @Size(min = 4, max = 50)
    private String username;
    @NotEmpty(message = "Please enter a password.")
    @Size(min = 4, max = 120)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "JwtLoginRequest{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
