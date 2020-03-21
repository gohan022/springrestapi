package com.gohan.springrestapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringrestapiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringrestapiApplication.class, args);
    }
}
