package com.bank.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_users_username",
            columnNames = "username"
        ),
        @UniqueConstraint(
            name = "uk_users_email",
            columnNames = "email"
        )
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        nullable = false,
        length = 100
    )
    private String fullName;

    @Column(
        nullable = false,
        unique = true,
        length = 50
    )
    private String username;

    @Column(
        nullable = false,
        unique = true,
        length = 100
    )
    private String email;

    /*
     * Store only the encoded password.
     * Never store plain-text passwords.
     */
    @Column(
        nullable = false,
        length = 255
    )
    private String password;

    /*
     * Store only the encoded ATM PIN.
     * Never store a plain-text PIN.
     *
     * Nullable so existing rows created before this
     * feature was added do not break.
     */
    @Column(
        length = 255
    )
    private String pin;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 20
    )
    private Role role = Role.USER;

    @Column(
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (role == null) {
            role = Role.USER;
        }
    }

    // =========================
    // Getters & Setters
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                "Full name is required."
            );
        }

        this.fullName = fullName.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                "Username is required."
            );
        }

        this.username = username.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                "Email is required."
            );
        }

        this.email = email.trim().toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                "Password is required."
            );
        }

        this.password = password;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {

        if (role == null) {
            throw new IllegalArgumentException(
                "User role is required."
            );
        }

        this.role = role;
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
}