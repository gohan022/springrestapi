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
    private Jwt jwt;
    private Cookie cookie;

    @Getter
    @Setter
    private static final class Jwt {
        @NotBlank
        private String tokenSecret;
        @NotBlank
        private Long tokenExpirationInMs;
        @NotBlank
        private Long refreshTokenExpirationInMs;
    }

    @Getter
    @Setter
    private static final class Cookie {
        @NotBlank
        private String accessTokenCookieName;
        @NotBlank
        private String refreshTokenCookieName;
        private boolean secure;
    }
}
