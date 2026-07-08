package com.example.Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// BE-05: @EnableScheduling added so LoginAttemptServiceImpl's @Scheduled
// cleanup task (which bounds the size of its in-memory attempt-tracking
// maps) actually runs. Nothing else in this codebase used @Scheduled
// before, so it wasn't enabled anywhere.
@EnableScheduling
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}