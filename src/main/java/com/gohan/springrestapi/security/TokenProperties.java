package com.gohan.springrestapi.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Configuration
@ConfigurationProperties(prefix = "api-auth")
@Getter
@Setter
public class TokenProperties {

    @NotBlank
    private String uri;
    @NotBlank
    private String refreshUri;
    @NotBlank
    private String destroyUri;
    private final Jwt jwt = new Jwt();

    @Getter
    @Setter
    public static class Jwt {
        @NotBlank
        private String tokenSecret;
        @NotBlank
        private String headerString;
        @NotBlank
        private String tokenPrefix;
        @NotBlank
        private Long tokenExpirationInMs;
        @NotBlank
        private Long refreshTokenExpirationInMs;
    }
}
