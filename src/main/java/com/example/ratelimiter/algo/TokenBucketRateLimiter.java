package com.example.ratelimiter.algo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

public class TokenBucketRateLimiter implements RateLimiter {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TokenBucketRateLimiter.class);
    private final int bucketCapacity;
    private final int refillRate;
    private final boolean enableRefill;
    private final ConcurrentHashMap<String, Integer> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TokenBucketRateLimiter(int bucketCapacity, int refillRate, boolean enableRefill) {
        this.bucketCapacity = bucketCapacity + 1;
        this.refillRate = refillRate;
        this.enableRefill = enableRefill;
        if (enableRefill)
            startRefilling();
    }

    private void startRefilling() {
        scheduler.scheduleAtFixedRate(() -> {
            logger.debug("Starting refill cycle...");
            buckets.forEach((apiKey, tokens) -> {
                int newTokens = Math.min(bucketCapacity, tokens + refillRate);
                logger.debug("Refilling API key: " + apiKey + ", Old: " + tokens + ", New: " + newTokens);
                buckets.put(apiKey, newTokens);
            });
            logger.debug("Buckets are refill: " + buckets);
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean allowRequest(String apiKey) {
        buckets.putIfAbsent(apiKey, bucketCapacity);
        logger.debug("API Key: " + apiKey + " initialized with tokens: " + buckets.get(apiKey));
        return buckets.compute(apiKey, (key, tokens) -> {
            if (tokens == null)
                tokens = bucketCapacity;
            logger.debug("API Key: " + apiKey + ", Before Request: " + tokens);
            int updatedTokens = tokens > 0 ? tokens - 1 : tokens;
            logger.debug("API Key: " + apiKey + ", After request: " + updatedTokens);
            return updatedTokens;
        }) > 0;
    }

}
