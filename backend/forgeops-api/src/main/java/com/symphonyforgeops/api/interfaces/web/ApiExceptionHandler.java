package com.symphonyforgeops.api.interfaces.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handle(ApiException exception) {
        return ResponseEntity
                .status(exception.status())
                .body(ApiResponse.fail(exception.code(), exception.getMessage()));
    }
}
