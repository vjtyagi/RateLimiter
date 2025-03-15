package com.example.ratelimiter.algo;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisTokenBucketRateLimiter implements RateLimiter {
    private final RedisTemplate<String, String> redisTemplate;
    private final int capacity;
    private final int refillRatePerSecond;

    public RedisTokenBucketRateLimiter(RedisTemplate<String, String> redisTemplate,
            @Value("${ratelimiter.bucket.capacity}") int capacity,
            @Value("${ratelimiter.refill-rate}") int refillRatePerSecond) {
        this.redisTemplate = redisTemplate;
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
    }

    @Override
    public boolean allowRequest(String apiKey) {
        String redisKey = "rate_limit:" + apiKey;
        long now = Instant.now().getEpochSecond();
        redisTemplate.opsForValue().setIfAbsent(redisKey + ":lastRefill", String.valueOf(now), 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().setIfAbsent(redisKey + ":tokens", String.valueOf(capacity), 1, TimeUnit.DAYS);

        long lastRefill = Long.parseLong(redisTemplate.opsForValue().get(redisKey + ":lastRefill"));
        long tokens = Long.parseLong(redisTemplate.opsForValue().get(redisKey + ":tokens"));

        long timeElapsed = now - lastRefill;
        long newTokens = Math.min(capacity, tokens + (timeElapsed * refillRatePerSecond));
        if (newTokens > 0) {
            redisTemplate.opsForValue().decrement(redisKey + ":tokens");
            return true;
        } else {
            return false;
        }
    }
}
