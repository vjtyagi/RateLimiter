package com.example.ratelimiter.algo;

import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisTokenBucketRateLimiter implements RateLimiter {
    private final RedisTemplate<String, String> redisTemplate;
    private final int capacity;
    private final double refillRatePerSecond;
    private final double refillRatePerMilliSecond;
    private final long ttl = 86400; // 1 day
    public static final String LUA_SCRIPT = """
                    -- Keys: [1] = base key for this API key
                    -- Args: [1] = current timestamp(ms), [2] = capacity, [3] = refill rate per ms, [4] = TTL

                    local baseKey = KEYS[1]
                    local tokensKey = baseKey .. ':tokens'
                    local lastRefillKey = baseKey .. ':lastRefill'

                    local now = tonumber(ARGV[1])
                    local capacity = tonumber(ARGV[2])
                    local refillRate = tonumber(ARGV[3])
                    local ttl = tonumber(ARGV[4])

                    -- Initialize Keys if they don't exist
                    if redis.call('EXISTS', tokensKey) == 0 then
                        redis.call('SET', tokensKey, capacity, 'EX', ttl)
                        redis.call('SET', lastRefillKey, now, 'EX', ttl)
                        -- Allow first request and consume one token
                        redis.call('DECR', tokensKey)
                        return 1
                    end

                    -- Get current values
                    local currentTokens = tonumber(redis.call('GET', tokensKey))
                    local lastRefill = tonumber(redis.call('GET', lastRefillKey))

                    -- Calculate tokens to add based on time elapsed (in milliseconds)
                    local timeElapsed = now - lastRefill
                    local tokensToAdd = math.floor(timeElapsed * refillRate)
                    local newTokens = math.min(capacity, currentTokens + tokensToAdd)

                    -- Update state if time has elapsed (refill occurred)
                    if timeElapsed > 0 then
                        redis.call('SET', tokensKey, newTokens, 'EX', ttl)
                        redis.call('SET', lastRefillKey, now, 'EX', ttl)
                    end

                    -- Check if we can consume a token
                    if newTokens >= 1 then
                        redis.call('DECR', tokensKey)
                        return 1 -- Request Allowed
                    else
                        return 0 -- Request Denied
                    end
            """;

    public RedisTokenBucketRateLimiter(RedisTemplate<String, String> redisTemplate,
            @Value("${ratelimiter.bucket.capacity}") int capacity,
            @Value("${ratelimiter.refill-rate}") double refillRatePerSecond) {
        this.redisTemplate = redisTemplate;
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.refillRatePerMilliSecond = refillRatePerSecond / 1000.0;
    }

    private static final RedisScript<Long> RATE_LIMIT_SCRIPT = RedisScript.of(LUA_SCRIPT, Long.class);

    @Override
    public boolean allowRequest(String apiKey) {
        String redisKey = "rate_limit:" + apiKey;
        long now = Instant.now().toEpochMilli();
        try {
            Long result = redisTemplate.execute(RATE_LIMIT_SCRIPT,
                    Collections.singletonList(redisKey), // keys
                    String.valueOf(now), // args
                    String.valueOf(capacity),
                    String.valueOf(refillRatePerMilliSecond),
                    String.valueOf(ttl));
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Rate limiter failed, allowing the request", e);
            return true;
        }

    }

    public boolean allowRequestUnsafe(String apiKey) {
        String redisKey = "rate_limit:" + apiKey;
        long now = Instant.now().toEpochMilli();
        redisTemplate.opsForValue().setIfAbsent(redisKey + ":lastRefill", String.valueOf(now), 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().setIfAbsent(redisKey + ":tokens", String.valueOf(capacity), 1, TimeUnit.DAYS);

        long lastRefill = Long.parseLong(redisTemplate.opsForValue().get(redisKey + ":lastRefill"));
        long tokens = Long.parseLong(redisTemplate.opsForValue().get(redisKey + ":tokens"));

        long timeElapsed = now - lastRefill;
        long newTokens = Math.min(capacity, tokens + (long) (timeElapsed * refillRatePerMilliSecond));
        if (timeElapsed > 0) {
            redisTemplate.opsForValue().set(redisKey + ":tokens", String.valueOf(newTokens));
            redisTemplate.opsForValue().set(redisKey + ":lastRefill", String.valueOf(now));
            tokens = newTokens;
        }
        if (tokens > 0) {
            redisTemplate.opsForValue().decrement(redisKey + ":tokens");
            return true;
        } else {
            return false;
        }
    }
}
