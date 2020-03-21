package com.gohan.springrestapi.user;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/*@Component*/
@Order(1)
public class UserSeeder implements CommandLineRunner {
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Role Seeder....");
        Role adminRole = new Role("ROLE_ADMIN");
        roleRepository.save(adminRole);
        Role userRole = new Role("ROLE_USER");
        roleRepository.save(userRole);

        System.out.println("User Seeder....");
        String adminPassword = encoder.encode("admin");
        String userPassword = encoder.encode("password");

        // Admin
        User admin = new User("Soumen", "Das", "admin", "admin@localhost.com", adminPassword);
        admin.addRole(adminRole);
        userRepository.save(admin);
        // User
        User user = new User("John", "Doe", "gohan022", "gohan044@gmail.com", userPassword);
        user.addRole(userRole);
        userRepository.save(user);

        // Other Users
        Faker faker = new Faker();

        for (int i = 0; i < 25; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();

            String username = faker.name().username();
            while (userRepository.findByUsername(username).isPresent()) {
                username = faker.name().username();
            }

            String email = faker.internet().emailAddress();
            while (userRepository.findByEmail(email).isPresent()) {
                email = faker.internet().emailAddress();
            }

            User newUser = new User(firstName, lastName, username, email, userPassword);
            newUser.addRole(userRole);

            userRepository.save(newUser);
        }
    }
}
