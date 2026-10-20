package com.quotaflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class QuotaFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuotaFlowApplication.class, args);
    }
}

