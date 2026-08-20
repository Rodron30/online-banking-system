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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_accounts_account_number",
                        columnNames = "account_number"
                ),
                @UniqueConstraint(
                        name = "uk_accounts_user_id",
                        columnNames = "user_id"
                )
        }
)
public class Account {

    private static final int ACCOUNT_NUMBER_LENGTH = 16;
    private static final int MONEY_SCALE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "account_number",
            nullable = false,
            unique = true,
            length = ACCOUNT_NUMBER_LENGTH
    )
    private String accountNumber;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_accounts_user"
            )
    )
    private User owner;

    @Column(
            nullable = false,
            precision = 19,
            scale = MONEY_SCALE
    )
    private BigDecimal balance =
            BigDecimal.ZERO.setScale(
                    MONEY_SCALE,
                    RoundingMode.UNNECESSARY
            );

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    /*
     * Soft-delete marker.
     *
     * NULL = account is active/visible.
     * Non-NULL = account was deleted by an admin.
     *
     * The account and its transaction history are kept
     * in the database for audit purposes, but the account
     * disappears from the Admin Dashboard and can no longer
     * be used to log in, deposit, withdraw, transfer,
     * or receive transfers.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (balance == null) {
            balance = BigDecimal.ZERO.setScale(
                    MONEY_SCALE,
                    RoundingMode.UNNECESSARY
            );
        } else {
            validateBalance(balance);

            balance = balance.setScale(
                    MONEY_SCALE,
                    RoundingMode.UNNECESSARY
            );
        }

        if (status == null) {
            status = AccountStatus.ACTIVE;
        }

        if (accountNumber == null
                || !accountNumber.matches("\\d{16}")) {

            throw new IllegalStateException(
                    "Account number must contain exactly 16 digits."
            );
        }

        if (owner == null
                || owner.getId() == null) {

            throw new IllegalStateException(
                    "Account owner is required."
            );
        }
    }

    private void validateBalance(BigDecimal value) {

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Account balance cannot be negative."
            );
        }

        if (value.scale() > MONEY_SCALE) {
            throw new IllegalArgumentException(
                    "Account balance may have up to 2 decimal places only."
            );
        }
    }

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {

        if (accountNumber == null) {
            throw new IllegalArgumentException(
                    "Account number is required."
            );
        }

        String normalized = accountNumber.trim();

        if (!normalized.matches("\\d{16}")) {
            throw new IllegalArgumentException(
                    "Account number must contain exactly 16 digits."
            );
        }

        this.accountNumber = normalized;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {

        if (owner == null) {
            throw new IllegalArgumentException(
                    "Account owner is required."
            );
        }

        this.owner = owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {

        if (balance == null) {
            throw new IllegalArgumentException(
                    "Account balance is required."
            );
        }

        validateBalance(balance);

        this.balance = balance.setScale(
                MONEY_SCALE,
                RoundingMode.UNNECESSARY
        );
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Account status is required."
            );
        }

        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "Creation date is required."
            );
        }

        this.createdAt = createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}