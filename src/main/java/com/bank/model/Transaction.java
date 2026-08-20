package com.bank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction {

    private static final int MONEY_SCALE = 2;

    /*
     * Bank account numbers are exactly 16 digits.
     */
    private static final int ACCOUNT_NUMBER_LENGTH = 16;

    private static final int MAX_DESCRIPTION_LENGTH = 255;

    private static final BigDecimal MINIMUM_AMOUNT =
            new BigDecimal("0.01");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transactions_account"
            )
    )
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private TransactionType type;

    @Column(
            nullable = false,
            precision = 19,
            scale = MONEY_SCALE
    )
    private BigDecimal amount;

    @Column(
            nullable = false,
            precision = 19,
            scale = MONEY_SCALE
    )
    private BigDecimal balanceAfter;

    /*
     * Account number on the other side of a transfer.
     *
     * NULL for deposits and withdrawals.
     *
     * Bank account number = exactly 16 digits.
     */
    @Column(
            name = "counterparty_account_number",
            length = ACCOUNT_NUMBER_LENGTH
    )
    private String counterpartyAccountNumber;

    @Column(
            length = MAX_DESCRIPTION_LENGTH
    )
    private String description;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime timestamp;

    // =========================================================
    // PRE PERSIST
    // =========================================================

    @PrePersist
    protected void onCreate() {

        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }

        validateTransaction();
    }

    // =========================================================
    // TRANSACTION VALIDATION
    // =========================================================

    private void validateTransaction() {

        if (account == null) {

            throw new IllegalStateException(
                    "Transaction account is required."
            );
        }

        if (type == null) {

            throw new IllegalStateException(
                    "Transaction type is required."
            );
        }

        validateAmount();

        validateBalanceAfter();

        validateCounterpartyAccountNumber();

        validateDescription();
    }

    // =========================================================
    // AMOUNT VALIDATION
    // =========================================================

    private void validateAmount() {

        if (amount == null) {

            throw new IllegalStateException(
                    "Transaction amount is required."
            );
        }

        if (amount.scale() > MONEY_SCALE) {

            throw new IllegalStateException(
                    "Transaction amount may have up to 2 decimal places only."
            );
        }

        if (amount.compareTo(MINIMUM_AMOUNT) < 0) {

            throw new IllegalStateException(
                    "Transaction amount must be at least ₱0.01."
            );
        }

        try {

            amount = amount.setScale(
                    MONEY_SCALE,
                    RoundingMode.UNNECESSARY
            );

        } catch (ArithmeticException e) {

            throw new IllegalStateException(
                    "Invalid transaction amount."
            );
        }
    }

    // =========================================================
    // BALANCE AFTER VALIDATION
    // =========================================================

    private void validateBalanceAfter() {

        if (balanceAfter == null) {

            throw new IllegalStateException(
                    "Balance after transaction is required."
            );
        }

        if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalStateException(
                    "Balance after transaction cannot be negative."
            );
        }

        if (balanceAfter.scale() > MONEY_SCALE) {

            throw new IllegalStateException(
                    "Balance may have up to 2 decimal places only."
            );
        }

        try {

            balanceAfter = balanceAfter.setScale(
                    MONEY_SCALE,
                    RoundingMode.UNNECESSARY
            );

        } catch (ArithmeticException e) {

            throw new IllegalStateException(
                    "Invalid balance after transaction."
            );
        }
    }

    // =========================================================
    // COUNTERPARTY ACCOUNT NUMBER VALIDATION
    // =========================================================

    private void validateCounterpartyAccountNumber() {

        /*
         * Deposits and withdrawals do not have
         * a counterparty account number.
         */
        if (counterpartyAccountNumber == null
                || counterpartyAccountNumber.isBlank()) {

            counterpartyAccountNumber = null;

            return;
        }

        String normalized =
                counterpartyAccountNumber.trim();

        /*
         * Must be exactly 16 digits.
         */
        if (!normalized.matches(
                "\\d{" + ACCOUNT_NUMBER_LENGTH + "}")) {

            throw new IllegalStateException(
                    "Counterparty account number must contain exactly "
                            + ACCOUNT_NUMBER_LENGTH
                            + " digits."
            );
        }

        counterpartyAccountNumber = normalized;
    }

    // =========================================================
    // DESCRIPTION VALIDATION
    // =========================================================

    private void validateDescription() {

        if (description == null) {

            return;
        }

        String normalized =
                description.trim();

        if (normalized.length()
                > MAX_DESCRIPTION_LENGTH) {

            throw new IllegalStateException(
                    "Description may contain up to "
                            + MAX_DESCRIPTION_LENGTH
                            + " characters only."
            );
        }

        description = normalized;
    }

    // =========================================================
    // GETTERS & SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {

        if (account == null) {

            throw new IllegalArgumentException(
                    "Transaction account is required."
            );
        }

        this.account = account;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {

        if (type == null) {

            throw new IllegalArgumentException(
                    "Transaction type is required."
            );
        }

        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {

        if (amount == null) {

            throw new IllegalArgumentException(
                    "Transaction amount is required."
            );
        }

        if (amount.scale() > MONEY_SCALE) {

            throw new IllegalArgumentException(
                    "Transaction amount may have up to 2 decimal places only."
            );
        }

        if (amount.compareTo(MINIMUM_AMOUNT) < 0) {

            throw new IllegalArgumentException(
                    "Transaction amount must be at least ₱0.01."
            );
        }

        this.amount = amount.setScale(
                MONEY_SCALE,
                RoundingMode.UNNECESSARY
        );
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(
            BigDecimal balanceAfter) {

        if (balanceAfter == null) {

            throw new IllegalArgumentException(
                    "Balance after transaction is required."
            );
        }

        if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Balance after transaction cannot be negative."
            );
        }

        if (balanceAfter.scale() > MONEY_SCALE) {

            throw new IllegalArgumentException(
                    "Balance may have up to 2 decimal places only."
            );
        }

        this.balanceAfter = balanceAfter.setScale(
                MONEY_SCALE,
                RoundingMode.UNNECESSARY
        );
    }

    public String getCounterpartyAccountNumber() {
        return counterpartyAccountNumber;
    }

    public void setCounterpartyAccountNumber(
            String counterpartyAccountNumber) {

        if (counterpartyAccountNumber == null
                || counterpartyAccountNumber.isBlank()) {

            this.counterpartyAccountNumber = null;

            return;
        }

        String normalized =
                counterpartyAccountNumber.trim();

        if (!normalized.matches(
                "\\d{" + ACCOUNT_NUMBER_LENGTH + "}")) {

            throw new IllegalArgumentException(
                    "Counterparty account number must contain exactly "
                            + ACCOUNT_NUMBER_LENGTH
                            + " digits."
            );
        }

        this.counterpartyAccountNumber = normalized;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {

        if (description == null) {

            this.description = null;

            return;
        }

        String normalized =
                description.trim();

        if (normalized.length()
                > MAX_DESCRIPTION_LENGTH) {

            throw new IllegalArgumentException(
                    "Description may contain up to "
                            + MAX_DESCRIPTION_LENGTH
                            + " characters only."
            );
        }

        this.description = normalized;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(
            LocalDateTime timestamp) {

        if (timestamp == null) {

            throw new IllegalArgumentException(
                    "Transaction timestamp is required."
            );
        }

        this.timestamp = timestamp;
    }
}
