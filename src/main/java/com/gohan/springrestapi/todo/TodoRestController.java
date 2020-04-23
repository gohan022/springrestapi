package com.gohan.springrestapi.todo;

import com.gohan.springrestapi.entities.todo.Todo;
import com.gohan.springrestapi.exceptions.MapValidationErrorService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// @CrossOrigin(origins = "http://localhost:4200")
@RestController
public class TodoRestController {
    private final TodoService todoService;
    private final MapValidationErrorService mapValidationErrorService;

    public TodoRestController(TodoService todoService, MapValidationErrorService mapValidationErrorService) {
        this.todoService = todoService;
        this.mapValidationErrorService = mapValidationErrorService;
    }

    @GetMapping("/user/todos")
    public Page<Todo> index(@RequestParam(required = false, defaultValue = "0") int page,
                            @RequestParam(required = false, defaultValue = "15") int size) {
        return todoService.findAll(PageRequest.of(page, size));
    }

    @GetMapping("/user/todos/{id}")
    public ResponseEntity<?> show(@PathVariable long id) {
        Map<String, Object> response = new HashMap<>();
        Todo todo;

        try {
            todo = todoService.findById(id);
        } catch (DataAccessException e) {
            response.put("message", "Failed to query the database!");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if(todo == null) {
            response.put("message", "The Todo ID: ".concat(Long.toString(id).concat(" does not exist in the database!")));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<Todo>(todo, HttpStatus.OK);
    }

    @PostMapping("/user/todos")
    public ResponseEntity<?> create(@RequestBody Todo todo, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        Todo todoNew;

        ResponseEntity<?> errorMap = mapValidationErrorService.MapValidationService(bindingResult);

        if(bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(err -> "Field "+ err.getDefaultMessage())
                    .collect(Collectors.toList());

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            todoNew = todoService.save(new Todo(todo.getDescription(), todo.getTargetDate()));
        } catch (DataAccessException e) {
            response.put("message", "Failed to insert into database!");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        /*final URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(todoNew.getId())
                                .toUri();*/

        response.put("message", "The Todo has been created successfully!");
        response.put("todo", todoNew);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/user/todos/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Todo todo, BindingResult bindingResult, @PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        /*
        ResponseEntity<?> responseFindById = this.show(id);
        Todo todoPrevious = (Todo) responseFindById.getBody();
        if(HttpStatus.NOT_FOUND.equals(responseFindById.getStatusCode()) || todoPrevious == null) {
            response.put("message", "Error: could not edit, todo ID: "
                    .concat(Long.toString(id).concat(" does not exist in the database!")));

            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        if(!HttpStatus.OK.equals(responseFindById.getStatusCode())) {
            return responseFindById;
        }
        */

        Todo todoPrevious;
        try {
            todoPrevious = todoService.findById(id);
        } catch (DataAccessException e) {
            response.put("message", "Failed to query the database!");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if(todoPrevious == null) {
            response.put("message", "Error: could not edit, todo ID: "
                    .concat(Long.toString(id).concat(" does not exist in the database!")));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
        }

        if(bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(err -> "Field "+ err.getDefaultMessage())
                    .collect(Collectors.toList());

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Todo todoUpdated;
        try {
            System.out.println(todo);
            todoPrevious.setDescription(todo.getDescription());
            todoPrevious.setTargetDate(todo.getTargetDate());
            todoPrevious.setDone(todo.isDone());

            todoUpdated = todoService.save(todoPrevious);
        } catch (DataAccessException e) {
            response.put("message", "Failed to update todo in database!");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("message", "The Todo has been successfully updated!");
        response.put("todo", todoUpdated);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/user/todos/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Todo todo = todoService.findById(id);
            if(todo == null) {
                response.put("message", "Error: could not delete, todo ID: "
                        .concat(Long.toString(id).concat(" does not exist in the database!")));
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            todoService.delete(id);
        } catch (DataAccessException e) {
            response.put("message", "Failed to delete todo from database");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("message", "The Todo successfully removed!");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
