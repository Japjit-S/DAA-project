package com.campus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for Smart Campus Navigation and Resource Optimizer.
 * Serves Spring Boot REST APIs and static frontend dashboard assets on port 8080.
 */
@SpringBootApplication
public class CampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusApplication.class, args);
    }
}
