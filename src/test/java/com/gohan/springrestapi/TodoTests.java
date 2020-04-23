package com.gohan.springrestapi;

import com.github.javafaker.Faker;
import com.gohan.springrestapi.entities.todo.Todo;
import com.gohan.springrestapi.entities.user.User;
import com.gohan.springrestapi.todo.TodoService;
import com.gohan.springrestapi.user.UserService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

@SpringBootTest
@ActiveProfiles("test")
public class TodoTests {
    private static final Logger logger = LoggerFactory.getLogger(SpringrestapiApplicationTests.class);

    @Autowired
    private TodoService todoService;
    @Autowired
    private UserService userService;

    private Faker faker = new Faker();

    @Test
    void testCreateTodo() {
        User user = userService.save(new User("john", "doe", "test", "test@localhost.com", "test"));

        Todo todo = new Todo(faker.lorem().sentence(2, 4), new Date());
        todo.setUser(user);
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
