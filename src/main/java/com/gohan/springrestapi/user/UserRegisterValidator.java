package com.gohan.springrestapi.user;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class UserRegisterValidator implements Validator {
    private final UserRepository userRepository;

    public UserRegisterValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return UserRegDTO.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserRegDTO user = (UserRegDTO) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "field.required", new Object[]{"First Name"}, "REQUIRED");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "field.required", new Object[]{"Last Name"}, "REQUIRED");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "field.required", new Object[]{"Username"}, "REQUIRED");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "field.required", new Object[]{"Email"}, "REQUIRED");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "field.required", new Object[]{"Password"}, "REQUIRED");

        if(!errors.hasFieldErrors("firstName")) {
            if (user.getFirstName().length() < 3 || user.getFirstName().length() > 100) {
                errors.rejectValue("firstName", "field.minMaxLength", new Object[]{"First Name", 3, 100}, "MIN_MAX_LENGTH");
            }
        }

        if(!errors.hasFieldErrors("lastName")) {
            if (user.getLastName().length() < 2 || user.getLastName().length() > 60) {
                errors.rejectValue("lastName", "field.minMaxLength", new Object[]{"Last Name", 2, 60}, "MIN_MAX_LENGTH");
            }
        }

        if(!errors.hasFieldErrors("username")) {
            if (user.getUsername().length() < 4 || user.getUsername().length() > 50) {
                errors.rejectValue("username", "field.minMaxLength", new Object[]{"Username", 4, 50}, "MIN_MAX_LENGTH");
            } else if(userRepository.findByUsername(user.getUsername()).isPresent()){
                errors.rejectValue("username", "field.alreadyExists", new Object[]{"Username"}, "ALREADY_EXISTS");
            }
        }

        if (!errors.hasFieldErrors("email")) {
            if(!EmailValidator.getInstance().isValid(user.getEmail())) {
                errors.rejectValue("email", "field.validEmail", new Object[]{"Email"}, "EMAIL_NOT_VALID");
            } else if(userRepository.findByEmail(user.getEmail()).isPresent()){
                errors.rejectValue("email", "field.alreadyExists", new Object[]{"Email"}, "ALREADY_EXISTS");
            }
        }

        if(!errors.hasFieldErrors("password")){
            if (user.getPassword().length() < 4 || user.getPassword().length() > 50) {
                errors.rejectValue("password", "field.minMaxLength", new Object[]{"Password", 6, 32}, "MIN_MAX_LENGTH");
            }
            else if (user.getConfirmPassword() == null  || !user.getConfirmPassword().equals(user.getPassword())){
                errors.rejectValue("confirmPassword", "field.passwordsMatch", "PASSWORD_NOT_MATCH");
            }
        }
    }
}
