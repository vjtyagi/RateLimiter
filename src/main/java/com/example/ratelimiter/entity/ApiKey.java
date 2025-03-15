package com.example.ratelimiter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "api_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_key", unique = true, nullable = false)
    private String apiKey;

    @Column(nullable = false)
    private boolean active;

    public ApiKey(String apiKey, boolean active) {
        this.apiKey = apiKey;
        this.active = active;
    }
}
