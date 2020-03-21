package com.gohan.springrestapi;

import com.github.javafaker.Faker;
import com.gohan.springrestapi.user.User;
import com.gohan.springrestapi.user.UserService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class UserTests {
    private static final Logger logger = LoggerFactory.getLogger(SpringrestapiApplicationTests.class);

    @Autowired
    private UserService userService;
    @Autowired
    private BCryptPasswordEncoder encoder;

    private Faker faker = new Faker();

    @Test
    void testCreateUser() {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String username = faker.name().username();
        String email = faker.internet().emailAddress();
        String password = encoder.encode("password");

        User user = new User(firstName, lastName, username, email, password);
        userService.save(user);
    }

    @Test
    void testFindUserById() {
        userService.findById(1L);
    }

    @Test
    void testUpdateUser() {
        User user = userService.findById(1L);
        if (user != null) {
            user.setUsername("gohan");
            user.setFirstName("Test");
            userService.save(user);
        }
    }

    @Test
    void testDeleteUser() {
        userService.delete(1L);
    }
}
