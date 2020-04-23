package com.gohan.springrestapi.todo;

import com.gohan.springrestapi.entities.todo.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {
}
