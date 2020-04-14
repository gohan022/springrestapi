package com.gohan.springrestapi;

import com.gohan.springrestapi.config.MyConfig;
import com.gohan.springrestapi.security.TokenProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class SpringrestapiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringrestapiApplication.class, args);
    }
}
