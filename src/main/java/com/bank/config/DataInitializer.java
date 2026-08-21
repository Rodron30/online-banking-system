package com.bank.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bank.model.Account;
import com.bank.model.AccountStatus;
import com.bank.model.Role;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import com.bank.service.AccountService;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeUsers(
            UserRepository userRepository,
            AccountRepository accountRepository,
            AccountService accountService,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // =====================================================
            // ADMIN ACCOUNT
            // =====================================================

            User admin = userRepository
                    .findByUsername("admin")
                    .orElseGet(() -> {

                        User user = new User();

                        user.setFullName("System Administrator");
                        user.setUsername("admin");
                        user.setEmail("admin@bank.com");
                        user.setPassword(
                                passwordEncoder.encode("admin123")
                        );
                        user.setPin(
                                passwordEncoder.encode("1234")
                        );
                        user.setRole(Role.ADMIN);

                        return userRepository.save(user);
                    });
            admin.setRole(Role.ADMIN);

            if (admin.getPassword() == null
                    || admin.getPassword().isBlank()) {
                admin.setPassword(
                        passwordEncoder.encode("admin123")
                );
            }

            if (admin.getPin() == null
                    || admin.getPin().isBlank()) {
                admin.setPin(
                        passwordEncoder.encode("1234")
                );
            }

            userRepository.save(admin);

            ensureDefaultAccount(
                    admin,
                    new BigDecimal("10000.00"),
                    "ADMIN",
                    accountRepository,
                    accountService
            );

            // =====================================================
            // DEMO USER
            // =====================================================

            User demo = userRepository
                    .findByUsername("demo")
                    .orElseGet(() -> {

                        User user = new User();

                        user.setFullName("Demo User");
                        user.setUsername("demo");
                        user.setEmail("demo@bank.com");
                        user.setPassword(
                                passwordEncoder.encode("Demo@123")
                        );
                        user.setPin(
                                passwordEncoder.encode("1234")
                        );
                        user.setRole(Role.USER);

                        return userRepository.save(user);
                    });

            demo.setRole(Role.USER);

            if (demo.getPassword() == null
                    || demo.getPassword().isBlank()) {
                demo.setPassword(
                        passwordEncoder.encode("Demo@123")
                );
            }

            if (demo.getPin() == null
                    || demo.getPin().isBlank()) {
                demo.setPin(
                        passwordEncoder.encode("1234")
                );
            }

            userRepository.save(demo);

            ensureDefaultAccount(
                    demo,
                    new BigDecimal("5000.00"),
                    "DEMO",
                    accountRepository,
                    accountService
            );

            // =====================================================
            // DEFAULT ACCOUNT INFORMATION
            // =====================================================

            System.out.println();
            System.out.println(
                    "========================================"
            );
            System.out.println(
                    "        DEFAULT ACCOUNTS READY"
            );
            System.out.println(
                    "========================================"
            );
            System.out.println(
                    "ADMIN → username: admin"
            );
            System.out.println(
                    "ADMIN → password: admin123"
            );
            System.out.println(
                    "DEMO  → username: demo"
            );
            System.out.println(
                    "DEMO  → password: Demo@123"
            );
            System.out.println(
                    "Default ATM PIN (both accounts): 1234"
            );
            System.out.println(
                    "========================================"
            );
        };
    }

    /**
     * Ensures the default account exists without ever attempting
     * to insert a second account for the same user.
     *
     * Accounts use a UNIQUE constraint on user_id, so a soft-deleted
     * account must also count as an existing account during startup.
     * If a default account was soft-deleted previously, restore that
     * existing row instead of inserting a duplicate. The existing
     * account number and balance are preserved.
     */
    private void ensureDefaultAccount(
            User user,
            BigDecimal openingBalance,
            String label,
            AccountRepository accountRepository,
            AccountService accountService) {

        Account existing = accountRepository
                .findByOwnerIncludingDeleted(user)
                .orElse(null);

        if (existing == null) {
            accountService.createAccountForUser(
                    user,
                    openingBalance
            );

            System.out.println(
                    label + " BANK ACCOUNT CREATED"
            );
            return;
        }

        if (existing.isDeleted()) {
            existing.setDeletedAt(null);
            existing.setStatus(AccountStatus.ACTIVE);
            accountRepository.save(existing);

            System.out.println(
                    label + " BANK ACCOUNT RESTORED"
            );
        }
    }

}
