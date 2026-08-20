package com.bank.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.model.NotificationType;
import com.bank.model.Role;
import com.bank.model.User;
import com.bank.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 72;

    private static final int MAX_FULL_NAME_LENGTH = 100;
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_EMAIL_LENGTH = 100;

    private static final int MIN_PIN_LENGTH = 4;
    private static final int MAX_PIN_LENGTH = 6;

    /**
     * Default PIN assigned to every newly registered user.
     * The user can change this using Change PIN.
     */
    private static final String DEFAULT_PIN = "1234";

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public UserService(
            UserRepository userRepository,
            AccountService accountService,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService) {

        this.userRepository = userRepository;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    // =========================================================
    // SPRING SECURITY
    // =========================================================

    /**
     * Load user for Spring Security authentication.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        if (username == null || username.isBlank()) {

            throw new UsernameNotFoundException(
                    "Username is required."
            );
        }

        String normalizedUsername =
                normalizeUsername(username);

        User user =
                userRepository
                        .findByUsername(normalizedUsername)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User not found."
                                )
                        );

        if (user.getRole() == null) {

            throw new UsernameNotFoundException(
                    "User has no valid role."
            );
        }

        if (user.getPassword() == null
                || user.getPassword().isBlank()) {

            throw new UsernameNotFoundException(
                    "User has no valid password."
            );
        }

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    // =========================================================
    // REGISTER
    // =========================================================

    /**
     * Register a normal USER.
     *
     * Every newly registered user automatically receives
     * one bank account with:
     *
     * - unique 16-digit account number
     * - zero opening balance
     * - ACTIVE status
     * - default PIN
     */
    @Transactional
    public User register(
            String fullName,
            String username,
            String email,
            String rawPassword) {

        String normalizedFullName =
                normalizeFullName(fullName);

        String normalizedUsername =
                normalizeUsername(username);

        String normalizedEmail =
                normalizeEmail(email);

        validatePassword(rawPassword);

        // -----------------------------------------------------
        // Check duplicate username
        // -----------------------------------------------------

        if (userRepository.existsByUsername(
                normalizedUsername)) {

            throw new IllegalArgumentException(
                    "Username is already in use."
            );
        }

        // -----------------------------------------------------
        // Check duplicate email
        // -----------------------------------------------------

        if (userRepository.existsByEmail(
                normalizedEmail)) {

            throw new IllegalArgumentException(
                    "Email address is already in use."
            );
        }

        // -----------------------------------------------------
        // Create USER
        // -----------------------------------------------------

        User user = new User();

        user.setFullName(
                normalizedFullName
        );

        user.setUsername(
                normalizedUsername
        );

        user.setEmail(
                normalizedEmail
        );

        /*
         * Always encode the password.
         * Never store the raw password.
         */
        user.setPassword(
                passwordEncoder.encode(
                        rawPassword
                )
        );

        /*
         * Public registration can only create
         * a normal USER.
         */
        user.setRole(
                Role.USER
        );

        /*
         * Every newly registered user receives
         * the default PIN 1234.
         *
         * The user can change it later.
         */
        user.setPin(
                passwordEncoder.encode(
                        DEFAULT_PIN
                )
        );

        // -----------------------------------------------------
        // Save USER first
        // -----------------------------------------------------

        User savedUser =
                userRepository.save(user);

        // -----------------------------------------------------
        // AUTOMATIC BANK ACCOUNT CREATION
        // -----------------------------------------------------

        /*
         * Every registered user must have one bank account.
         *
         * AccountService automatically generates:
         *
         * - unique 16-digit account number
         * - zero opening balance
         * - ACTIVE status
         */
        accountService.createAccountForUser(
                savedUser,
                BigDecimal.ZERO
        );

        return savedUser;
    }

    // =========================================================
    // CHANGE PIN
    // =========================================================

    /**
     * Change the ATM PIN of a user.
     */
    @Transactional
    public void changePin(
            String username,
            String currentPin,
            String newPin,
            String confirmNewPin) {

        if (newPin == null
                || confirmNewPin == null
                || !newPin.equals(confirmNewPin)) {

            throw new IllegalArgumentException(
                    "New PIN and confirmation do not match."
            );
        }

        validatePin(newPin);

        String normalizedUsername =
                normalizeUsername(username);

        User user =
                userRepository
                        .findByUsername(
                                normalizedUsername
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found."
                                )
                        );

        /*
         * Verify current PIN when one already exists.
         */
        if (user.getPin() != null
                && !user.getPin().isBlank()) {

            if (currentPin == null
                    || currentPin.isBlank()
                    || !passwordEncoder.matches(
                            currentPin,
                            user.getPin()
                    )) {

                throw new IllegalArgumentException(
                        "Current PIN is incorrect."
                );
            }
        }

        user.setPin(
                passwordEncoder.encode(
                        newPin
                )
        );

        userRepository.save(user);

        notificationService.notify(
                user,
                NotificationType.PIN_CHANGED,
                "Your ATM PIN was changed successfully."
        );
    }

    // =========================================================
    // GET USER
    // =========================================================

    /**
     * Get a user by username.
     */
    @Transactional(readOnly = true)
    public User getByUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            throw new IllegalArgumentException(
                    "Username is required."
            );
        }

        String normalizedUsername =
                normalizeUsername(username);

        return userRepository
                .findByUsername(
                        normalizedUsername
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found."
                        )
                );
    }

    // =========================================================
    // GET ALL USERS
    // =========================================================

    /**
     * Get all users.
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    // =========================================================
    // FULL NAME VALIDATION
    // =========================================================

    /**
     * Validate and normalize full name.
     */
    private String normalizeFullName(
            String fullName) {

        if (fullName == null
                || fullName.isBlank()) {

            throw new IllegalArgumentException(
                    "Full name is required."
            );
        }

        String normalized =
                fullName.trim();

        if (normalized.length()
                > MAX_FULL_NAME_LENGTH) {

            throw new IllegalArgumentException(
                    "Full name must not exceed "
                            + MAX_FULL_NAME_LENGTH
                            + " characters."
            );
        }

        return normalized;
    }

    // =========================================================
    // USERNAME VALIDATION
    // =========================================================

    /**
     * Validate and normalize username.
     *
     * Allowed:
     * - letters
     * - numbers
     * - underscore
     * - dot
     * - hyphen
     */
    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            throw new IllegalArgumentException(
                    "Username is required."
            );
        }

        String normalized =
                username
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (normalized.length() < 3) {

            throw new IllegalArgumentException(
                    "Username must be at least 3 characters."
            );
        }

        if (normalized.length()
                > MAX_USERNAME_LENGTH) {

            throw new IllegalArgumentException(
                    "Username must not exceed "
                            + MAX_USERNAME_LENGTH
                            + " characters."
            );
        }

        if (!normalized.matches(
                "[a-z0-9._-]+")) {

            throw new IllegalArgumentException(
                    "Username may only contain "
                            + "letters, numbers, dot, "
                            + "underscore, and hyphen."
            );
        }

        return normalized;
    }

    // =========================================================
    // EMAIL VALIDATION
    // =========================================================

    /**
     * Validate and normalize email.
     */
    private String normalizeEmail(
            String email) {

        if (email == null
                || email.isBlank()) {

            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        String normalized =
                email
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (normalized.length()
                > MAX_EMAIL_LENGTH) {

            throw new IllegalArgumentException(
                    "Email must not exceed "
                            + MAX_EMAIL_LENGTH
                            + " characters."
            );
        }

        if (!normalized.matches(
                "^[A-Za-z0-9._%+-]+"
                        + "@[A-Za-z0-9.-]+"
                        + "\\.[A-Za-z]{2,}$")) {

            throw new IllegalArgumentException(
                    "Invalid email address."
            );
        }

        return normalized;
    }

    // =========================================================
    // PIN VALIDATION
    // =========================================================

    /**
     * Validate PIN.
     *
     * Must contain 4 to 6 digits.
     */
    private void validatePin(
            String pin) {

        if (pin == null
                || pin.isBlank()) {

            throw new IllegalArgumentException(
                    "PIN is required."
            );
        }

        if (!pin.matches(
                "\\d{"
                        + MIN_PIN_LENGTH
                        + ","
                        + MAX_PIN_LENGTH
                        + "}")) {

            throw new IllegalArgumentException(
                    "PIN must be "
                            + MIN_PIN_LENGTH
                            + " to "
                            + MAX_PIN_LENGTH
                            + " digits."
            );
        }
    }

    // =========================================================
    // PASSWORD VALIDATION
    // =========================================================

    /**
     * Validate password before BCrypt encoding.
     */
    private void validatePassword(
            String password) {

        if (password == null
                || password.isBlank()) {

            throw new IllegalArgumentException(
                    "Password is required."
            );
        }

        if (password.length()
                < MIN_PASSWORD_LENGTH) {

            throw new IllegalArgumentException(
                    "Password must be at least "
                            + MIN_PASSWORD_LENGTH
                            + " characters."
            );
        }

        /*
         * BCrypt has a practical maximum
         * input length of 72 bytes.
         */
        if (password.length()
                > MAX_PASSWORD_LENGTH) {

            throw new IllegalArgumentException(
                    "Password must not exceed "
                            + MAX_PASSWORD_LENGTH
                            + " characters."
            );
        }
    }
}