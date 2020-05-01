package com.gohan.springrestapi.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@Service
public class MapValidationErrorService {

    public ResponseEntity<?> MapValidationService(BindingResult result){

        if(result.hasErrors()){
            Map<String, Object> response = new HashMap<>();
            Map<String, Map<String, Object>> errorMap = new HashMap<>();

            for(FieldError error: result.getFieldErrors()) {
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("field", error.getField());
                errorDetails.put("message", error.getDefaultMessage());
                errorDetails.put("arguments", error.getArguments());
                errorMap.put(error.getField(), errorDetails);
            }
            response.put("errors", errorMap);
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return null;

    }
}
