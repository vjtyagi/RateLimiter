package com.example.ratelimiter.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ratelimiter.entity.ApiKey;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByApiKey(String apiKey);

    boolean existsByApiKey(String key);
}
