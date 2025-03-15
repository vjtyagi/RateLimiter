package com.example.ratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication()
public class RatelimiterApplication {

	public static void main(String[] args) {
		SpringApplication.run(RatelimiterApplication.class, args);
	}

}
