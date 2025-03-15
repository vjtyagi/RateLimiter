package com.example.ratelimiter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class RedisConnectionTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void testRedisConnection() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        // Store a value
        ops.set("testKey", "testValue");
        // Retrieve and check the value
        String value = ops.get("testKey");
        assertEquals(value, "testValue");
    }
}
