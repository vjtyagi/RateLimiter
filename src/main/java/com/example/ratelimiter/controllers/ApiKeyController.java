package com.example.ratelimiter.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ratelimiter.service.ApiKeyService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/keys")
public class ApiKeyController {
    private ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateKey() {
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.createNewApiKey().getApiKey());
    }

}
