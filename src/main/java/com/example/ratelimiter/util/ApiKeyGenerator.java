package com.example.ratelimiter.util;

import java.security.SecureRandom;
import java.util.Base64;

public class ApiKeyGenerator {
    /*
     * SecureRandom : cryptographically strong random no. generator
     * Unpredictable, designed for security, good for security related tasks(eg api
     * keys, passwords etc)
     * Seed: uses system entropy(os randomness)
     */
    private static final SecureRandom random = new SecureRandom();

    public static String generateApiKey() {
        // Generate 32 random bytes
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        /*
         * Convert bytes into base64 string
         * withoutPadding removes trailing "="
         * base64: Bcz compatible with URLs, headers and databases
         */
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

}
