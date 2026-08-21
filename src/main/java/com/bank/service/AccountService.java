package com.bank.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.model.Account;
import com.bank.model.AccountStatus;
import com.bank.model.User;
import com.bank.repository.AccountRepository;

@Service
public class AccountService {

    private static final int ACCOUNT_NUMBER_LENGTH = 16;
    private static final int MONEY_SCALE = 2;

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private final AccountRepository accountRepository;

    public AccountService(
            AccountRepository accountRepository) {

        this.accountRepository = accountRepository;
    }

    // =========================================================
    // CREATE ACCOUNT
    // =========================================================

    @Transactional
    public Account createAccountForUser(
            User user,
            BigDecimal openingBalance) {

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "Invalid user."
            );
        }

        BigDecimal validOpeningBalance =
                normalizeOpeningBalance(openingBalance);

        /*
         * One account per user.
         *
         * IMPORTANT: include soft-deleted accounts in this check.
         * The database has a UNIQUE constraint on accounts.user_id,
         * so a soft-deleted row still occupies that user_id.
         * Checking only active accounts could otherwise cause a
         * PostgreSQL duplicate-key exception.
         */
        if (accountRepository
                .findByOwnerIncludingDeleted(user)
                .isPresent()) {

            throw new IllegalStateException(
                    "This user already has a bank account. "
                    + "The existing account must be restored or used instead."
            );
        }

        Account account = new Account();

        account.setOwner(user);

        account.setAccountNumber(
                generateUniqueAccountNumber()
        );

        account.setBalance(
                validOpeningBalance
        );

        account.setStatus(
                AccountStatus.ACTIVE
        );

        return accountRepository.save(account);
    }

    // =========================================================
    // GENERATE UNIQUE 16-DIGIT ACCOUNT NUMBER
    // =========================================================

    private String generateUniqueAccountNumber() {

        String candidate;

        do {

            StringBuilder builder =
                    new StringBuilder(
                            ACCOUNT_NUMBER_LENGTH
                    );

            /*
             * First digit: 1-9
             * This prevents leading zero.
             */
            builder.append(
                    RANDOM.nextInt(9) + 1
            );

            /*
             * Remaining 15 digits: 0-9
             */
            for (int i = 1;
                 i < ACCOUNT_NUMBER_LENGTH;
                 i++) {

                builder.append(
                        RANDOM.nextInt(10)
                );
            }

            candidate = builder.toString();

        } while (
                accountRepository
                        .existsByAccountNumber(candidate)
        );

        return candidate;
    }

    // =========================================================
    // GET ACCOUNT BY USERNAME
    // =========================================================

    @Transactional(readOnly = true)
    public Account getAccountForUsername(
            String username) {

        String normalizedUsername =
                normalizeUsername(username);

        return accountRepository
                .findByOwnerUsername(normalizedUsername)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No bank account found for the specified user."
                        )
                );
    }

    // =========================================================
    // GET ACCOUNT BY ACCOUNT NUMBER
    // =========================================================

    @Transactional(readOnly = true)
    public Account getByAccountNumber(
            String accountNumber) {

        String normalizedAccountNumber =
                normalizeAccountNumber(accountNumber);

        return accountRepository
                .findByAccountNumber(
                        normalizedAccountNumber
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account number not found."
                        )
                );
    }

    // =========================================================
    // GET ALL ACCOUNTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {

        return accountRepository.findAllWithOwner();
    }

    // =========================================================
    // GET ACCOUNT BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public Account getById(Long accountId) {

        if (accountId == null) {
            throw new IllegalArgumentException(
                    "Invalid account ID."
            );
        }

        return accountRepository
                .findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found."
                        )
                );
    }

    // =========================================================
    // CHANGE ACCOUNT STATUS
    // =========================================================

    @Transactional
    public void setStatus(
            Long accountId,
            AccountStatus status) {

        if (accountId == null) {
            throw new IllegalArgumentException(
                    "Invalid account ID."
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "Account status is required."
            );
        }

        Account account =
                accountRepository
                        .findById(accountId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found."
                                )
                        );

        account.setStatus(status);

        accountRepository.save(account);
    }

    // =========================================================
    // DELETE ACCOUNT (SOFT DELETE)
    // =========================================================

    /**
     * Soft-delete an account.
     *
     * The account and its transaction history stay in the
     * database for audit purposes, but:
     *
     * - it disappears from the Admin Dashboard
     * - it can no longer be used to log in, deposit,
     *   withdraw, transfer, or receive transfers
     */
    @Transactional
    public void deleteAccount(Long accountId) {

        if (accountId == null) {
            throw new IllegalArgumentException(
                    "Invalid account ID."
            );
        }

        Account account =
                accountRepository
                        .findById(accountId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found."
                                )
                        );

        if (account.isDeleted()) {

            throw new IllegalStateException(
                    "This account is already deleted."
            );
        }

        account.setDeletedAt(LocalDateTime.now());

        accountRepository.save(account);
    }

    // =========================================================
    // OPENING BALANCE
    // =========================================================

    private BigDecimal normalizeOpeningBalance(
            BigDecimal openingBalance) {

        if (openingBalance == null) {

            return BigDecimal.ZERO.setScale(
                    MONEY_SCALE,
                    RoundingMode.UNNECESSARY
            );
        }

        if (openingBalance.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Opening balance cannot be negative."
            );
        }

        if (openingBalance.scale()
                > MONEY_SCALE) {

            throw new IllegalArgumentException(
                    "Opening balance can have a maximum of 2 decimal places."
            );
        }

        return openingBalance.setScale(
                MONEY_SCALE,
                RoundingMode.UNNECESSARY
        );
    }

    // =========================================================
    // ACCOUNT NUMBER VALIDATION
    // =========================================================

    private String normalizeAccountNumber(
            String accountNumber) {

        if (accountNumber == null
                || accountNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Account number is required."
            );
        }

        String normalized =
                accountNumber.trim();

        if (!normalized.matches("\\d{16}")) {

            throw new IllegalArgumentException(
                    "Account number must contain exactly 16 digits."
            );
        }

        return normalized;
    }

    // =========================================================
    // USERNAME VALIDATION
    // =========================================================

    private String normalizeUsername(
            String username) {

        if (username == null
                || username.isBlank()) {

            throw new IllegalArgumentException(
                    "Username is required."
            );
        }

        return username
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}