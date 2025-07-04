package com.example.ratelimiter.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class TestController {
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test Endpoint");
    }

}
