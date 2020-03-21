package com.gohan.springrestapi.todo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TodoService {
    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Transactional(readOnly = true)
    public List<Todo> findAll() {
        return todoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Todo findById(Long id) {
        return todoRepository.findById(id).orElse(null);
    }

    @Transactional
    public Todo save(Todo todo) {
        return todoRepository.save(todo);
    }

    @Transactional
    public void delete(Long id) {
        todoRepository.deleteById(id);
    }
}
