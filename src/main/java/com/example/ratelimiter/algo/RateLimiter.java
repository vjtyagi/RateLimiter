package com.example.ratelimiter.algo;

public interface RateLimiter {
    boolean allowRequest(String apiKey);
}
