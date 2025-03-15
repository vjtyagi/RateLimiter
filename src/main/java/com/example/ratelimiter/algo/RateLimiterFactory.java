package com.example.ratelimiter.algo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RateLimiterFactory {

    private final TokenBucketRateLimiter tokenBucketRateLimiter;
    private final RedisTokenBucketRateLimiter redisTokenBucketRateLimiter;

    public RateLimiterFactory(@Value("${ratelimiter.bucket.capacity}") int bucketCapacity,
            @Value("${ratelimiter.refill-rate}") int refillRate,
            @Value("${ratelimiter.refill.enabled:true}") boolean enableRefill,
            RedisTokenBucketRateLimiter redisTokenBucketRateLimiter) {

        this.tokenBucketRateLimiter = new TokenBucketRateLimiter(bucketCapacity, refillRate, enableRefill);
        this.redisTokenBucketRateLimiter = redisTokenBucketRateLimiter;
    }

    public RateLimiter getRateLimiter(String type) {
        return switch (type.toLowerCase()) {
            case "token-bucket" -> tokenBucketRateLimiter;
            case "redis" -> redisTokenBucketRateLimiter;
            default -> throw new IllegalArgumentException("Unknown rate limiter type: " + type);

        };
    }
}
