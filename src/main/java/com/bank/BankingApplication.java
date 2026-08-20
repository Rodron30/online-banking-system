package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * Default account seeding (admin + demo user) is handled by
 * {@link com.bank.config.DataInitializer} so there is only
 * ONE source of truth for seed data and default credentials.
 */
@SpringBootApplication
public class BankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }
}
