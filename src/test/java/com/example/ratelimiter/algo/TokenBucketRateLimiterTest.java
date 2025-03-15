package com.example.ratelimiter.algo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TokenBucketRateLimiterTest {
    private TokenBucketRateLimiter rateLimiter;

    @BeforeEach
    void setup() {
        rateLimiter = new TokenBucketRateLimiter(5, 1, false);
    }

    @Test
    void shouldAllowRequestWhenTokensAvailable() {
        assertTrue(rateLimiter.allowRequest("test-key"));
        assertTrue(rateLimiter.allowRequest("test-key"));
        assertTrue(rateLimiter.allowRequest("test-key"));
    }

    @Test
    void shouldDenyRequestWhenTokensExhausted() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.allowRequest("test-key");
        }
        assertFalse(rateLimiter.allowRequest("test-key")); // 6th request should fail
    }

    @Test
    void shouldRefillTokensOvertime() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.allowRequest("test-key");
        }
        assertFalse(rateLimiter.allowRequest("test-key"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(rateLimiter.allowRequest("test-key"));
    }
}
