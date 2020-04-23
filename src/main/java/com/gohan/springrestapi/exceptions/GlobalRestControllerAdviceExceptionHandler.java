package com.gohan.springrestapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

// @RestControllerAdvice
public class GlobalRestControllerAdviceExceptionHandler {

    /*@ExceptionHandler(UserNameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse usernameNotFound(UserNameNotFoundException ex) {
        return new ErrorResponse(new Date(), true, HttpStatus.BAD_REQUEST, ex.getMessage());
    }*/
}
