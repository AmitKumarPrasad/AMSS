package com.edusphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EdusphereApplication {
    public static void main(String[] args) {
        SpringApplication.run(EdusphereApplication.class, args);
    }
}
