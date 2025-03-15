package com.example.ratelimiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.ratelimiter.algo.RateLimiterFactory;
import com.example.ratelimiter.filter.ApiKeyAuthFilter;
import com.example.ratelimiter.service.ApiKeyService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final ApiKeyService apiKeyService;
    private final RateLimiterFactory rateLimiterFactory;

    public SecurityConfig(ApiKeyService apiKeyService, RateLimiterFactory rateLimiterFactory) {
        this.apiKeyService = apiKeyService;
        this.rateLimiterFactory = rateLimiterFactory;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/h2-console/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/keys/generate")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new ApiKeyAuthFilter(apiKeyService, rateLimiterFactory),
                        UsernamePasswordAuthenticationFilter.class)
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());
        return http.build();
    }
}
