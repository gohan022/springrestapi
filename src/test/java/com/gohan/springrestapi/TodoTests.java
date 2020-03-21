package com.gohan.springrestapi;

import com.github.javafaker.Faker;
import com.gohan.springrestapi.todo.Todo;
import com.gohan.springrestapi.todo.TodoService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class TodoTests {
    private static final Logger logger = LoggerFactory.getLogger(SpringrestapiApplicationTests.class);

    @Autowired
    private TodoService todoService;

    private Faker faker = new Faker();

    @Test
    void testCreateTodo() {
        Todo todo = new Todo(faker.lorem().sentence(2, 4));
        todoService.save(todo);
    }

    @Test
    void testFindTodoById() {
        todoService.findById(1L);
    }

    @Test
    void testUpdateTodo() {
        Todo todo = todoService.findById(1L);
        if (todo != null) {
            todo.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
            todo.setDone(true);
            todoService.save(todo);
        }
    }

    @Test
    void testDeleteUser() {
        todoService.delete(1L);
    }
}
