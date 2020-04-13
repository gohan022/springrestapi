package com.gohan.springrestapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@Configuration
@ConfigurationProperties("springrestapi")
@EnableJpaAuditing
@Getter
@Setter
public class MyConfig {

    @Bean
    public AccountStatusUserDetailsChecker accountStatusUserDetailsChecker() {
        return new AccountStatusUserDetailsChecker();
    }
}
