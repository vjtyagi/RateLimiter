package com.example.ratelimiter.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.ratelimiter.dao.ApiKeyRepository;
import com.example.ratelimiter.entity.ApiKey;
import com.example.ratelimiter.util.ApiKeyGenerator;

@Service
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public ApiKey createNewApiKey() {
        String apiKey;
        do {
            apiKey = ApiKeyGenerator.generateApiKey();
        } while (apiKeyRepository.existsByApiKey(apiKey));

        return apiKeyRepository.save(new ApiKey(apiKey, true));
    }

    public boolean isValidApiKey(String key) {
        Optional<ApiKey> apiKey = apiKeyRepository.findByApiKey(key);
        return apiKey.isPresent() && apiKey.get().isActive();
    }

    public void deactivateKey(String key) {
        apiKeyRepository.findByApiKey(key).ifPresent(apiKey -> {
            apiKey.setActive(false);
            apiKeyRepository.save(apiKey);
        });
    }
}
