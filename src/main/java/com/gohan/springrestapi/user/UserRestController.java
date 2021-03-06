package com.gohan.springrestapi.user;

import com.gohan.springrestapi.entities.Role;
import com.gohan.springrestapi.entities.User;
import com.gohan.springrestapi.service.MapValidationErrorService;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataAccessException;
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

@RestController
public class UserRestController {
    private final UserService userService;
    private final UserRegisterValidator userRegisterValidator;
    private final MapValidationErrorService errorService;
    private final ModelMapper modelMapper;

    public UserRestController(UserService userService, UserRegisterValidator userRegisterValidator, MapValidationErrorService errorService, ModelMapper modelMapper) {
        this.userService = userService;
        this.userRegisterValidator = userRegisterValidator;
        this.errorService = errorService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/admin/users")
    public List<User> index() {
        return userService.findAll();
    }

    @GetMapping("/admin/users/{id}")
    public ResponseEntity<?> show(@PathVariable long id) {
        Map<String, Object> response = new HashMap<>();
        User user;

        try {
            user = userService.findById(id);
        } catch (DataAccessException e) {
            response.put("message", "Failed to query the database!");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (user == null) {
            response.put("message", "The User ID: ".concat(Long.toString(id).concat(" does not exist in the database!")));
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/admin/users")
    public ResponseEntity<?> create(@Valid @RequestBody User user, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        User newUser;

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(err -> "Field " + err.getDefaultMessage())
                    .collect(Collectors.toList());

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            user.setRole(Role.USER);
            newUser = userService.save(user);
        } catch (DataAccessException e) {
            response.put("message", "Failed to insert into database!");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("message", "The User has been created successfully!");
        response.put("user", newUser);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/admin/users/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody User user, BindingResult bindingResult, @PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        User prevUser;

        try {
            prevUser = userService.findById(id);
        } catch (DataAccessException e) {
            response.put("message", "Failed to query the database!");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (prevUser == null) {
            response.put("message", "Error: could not edit, user ID: "
                    .concat(Long.toString(id).concat(" does not exist in the database!")));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
        }

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(err -> "Field " + err.getDefaultMessage())
                    .collect(Collectors.toList());

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User userUpdated;
        try {
            user.setId(prevUser.getId());

            userUpdated = userService.save(user);
        } catch (DataAccessException e) {
            response.put("message", "Failed to update user in database!");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("message", "The User has been successfully updated!");
        response.put("user", userUpdated);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userService.findById(id);
            if (user == null) {
                response.put("message", "Error: could not delete, user ID: "
                        .concat(Long.toString(id).concat(" does not exist in the database!")));
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            userService.delete(id);
        } catch (DataAccessException e) {
            response.put("message", "Failed to delete user from database");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("message", "The User has been successfully removed!");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegDTO userDTO, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        this.userRegisterValidator.validate(userDTO, bindingResult);
        ResponseEntity<?> errorsList = this.errorService.MapValidationService(bindingResult);
        if (errorsList != null) return errorsList;

        try {
            User user = modelMapper.map(userDTO, User.class);
            userService.register(user);
        } catch (DataAccessException e) {
            response.put("message", "Failed to insert into database!");
            response.put("error", Objects.requireNonNull(e.getMessage()).concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("message", "Registration successfully!");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
