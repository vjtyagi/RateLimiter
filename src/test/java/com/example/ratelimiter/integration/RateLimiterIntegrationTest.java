package com.example.ratelimiter.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.ratelimiter.dao.ApiKeyRepository;
import com.example.ratelimiter.service.ApiKeyService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "ratelimiter.refill.eanbled=false")
public class RateLimiterIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    private String testApiKey;

    @BeforeEach
    void setup() throws Exception {
        // clear db before each test
        apiKeyRepository.deleteAll();

        // Step1: Generate a new api key
        String response = mockMvc.perform(
                post("/api/keys/generate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        testApiKey = response;

    }

    @Test
    void rateLimiting() throws Exception {
        String authHeader = "Bearer " + testApiKey;
        // Make requests within the limit
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/test")
                    .header("Authorization", authHeader))
                    .andExpect(status().isOk());
        }
        // Exceed the rate limit
        mockMvc.perform(get("/test")
                .header("Authorization", authHeader))
                .andExpect(status().isTooManyRequests());
    }
}
