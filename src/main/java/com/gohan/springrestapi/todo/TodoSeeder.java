package com.gohan.springrestapi.todo;

import com.github.javafaker.Faker;
import com.gohan.springrestapi.entities.Todo;
import com.gohan.springrestapi.entities.User;
import com.gohan.springrestapi.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;

/*@Component*/
@Order(2)
public class TodoSeeder implements CommandLineRunner {
    @Autowired
    private TodoRepository todoRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        Faker faker = new Faker();

        System.out.println("Todo Seeder....");

        Random rand = new Random();
        List<User> users = userRepository.findAll();
        for (int i = 0; i < 15; i++) {
            User user = users.get(rand.nextInt(users.size()));

            String description = faker.lorem().sentence();
            Todo todo = new Todo(description, new Date());
            todo.setUser(user);
            todoRepository.save(todo);
        }
    }
}
