package com.example.ratelimiter.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.ratelimiter.algo.RateLimiter;
import com.example.ratelimiter.algo.RateLimiterFactory;
import com.example.ratelimiter.exceptions.RateLimitExceededException;
import com.example.ratelimiter.service.ApiKeyService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private ApiKeyService apiKeyService;
    private RateLimiterFactory rateLimiterFactory;

    public ApiKeyAuthFilter(ApiKeyService apiKeyService, RateLimiterFactory rateLimiterFactory) {
        this.apiKeyService = apiKeyService;
        this.rateLimiterFactory = rateLimiterFactory;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String apiKey = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (apiKey != null && apiKey.startsWith("Bearer ")) {
                apiKey = apiKey.substring(7);
                if (apiKeyService.isValidApiKey(apiKey)) {
                    var authToken = new PreAuthenticatedAuthenticationToken(apiKey, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    checkRateLimit(apiKey);
                }

            }
            filterChain.doFilter(request, response);
        } catch (RateLimitExceededException e) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("Rate limit exceeded. Please try again later.");
        }

    }

    private void checkRateLimit(String apiKey) {
        RateLimiter rateLimiter = rateLimiterFactory.getRateLimiter("redis");
        if (!rateLimiter.allowRequest(apiKey)) {
            throw new RateLimitExceededException("Rate Limit Exceeded for API Key : " + apiKey);
        }
    }
}
