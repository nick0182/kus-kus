package com.shaidulin.kuskus.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class ConstraintViolationHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleConstraintViolationException(WebExchangeBindException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
