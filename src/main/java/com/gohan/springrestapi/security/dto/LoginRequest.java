package com.gohan.springrestapi.security.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class LoginRequest {
    @NotEmpty(message = "Please enter a username.")
    @Size(min = 4, max = 50)
    private String username;

    @NotEmpty(message = "Please enter a password.")
    private String password;

    private boolean rememberMe;
}
