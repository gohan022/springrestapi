package com.gohan.springrestapi.todo;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/*@Component*/
@Order(2)
public class TodoSeeder implements CommandLineRunner {
    @Autowired
    private TodoRepository todoRepository;

    @Override
    public void run(String... args) throws Exception {
        Faker faker = new Faker();

        System.out.println("Todo Seeder....");

        for (int i = 0; i < 15; i++) {
            String description = faker.lorem().sentence();
            todoRepository.save(new Todo(description));
        }
    }
}
