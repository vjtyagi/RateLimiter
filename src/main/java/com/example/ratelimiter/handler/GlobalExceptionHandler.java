package com.example.ratelimiter.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.ratelimiter.exceptions.RateLimitExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<String> handleRateLimitExceededException(RateLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded. Please try again later.");
    }
}
