package com.bank.model;

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
@Table(name = "notifications")
public class Notification {

    private static final int MAX_MESSAGE_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_notifications_user"
            )
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private NotificationType type;

    @Column(
            nullable = false,
            length = MAX_MESSAGE_LENGTH
    )
    private String message;

    @Column(
            name = "is_read",
            nullable = false
    )
    private boolean isRead = false;

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

        if (message != null
                && message.length() > MAX_MESSAGE_LENGTH) {

            message = message.substring(
                    0,
                    MAX_MESSAGE_LENGTH
            );
        }
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {

        if (user == null) {

            throw new IllegalArgumentException(
                    "Notification user is required."
            );
        }

        this.user = user;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {

        if (type == null) {

            throw new IllegalArgumentException(
                    "Notification type is required."
            );
        }

        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {

        if (message == null || message.isBlank()) {

            throw new IllegalArgumentException(
                    "Notification message is required."
            );
        }

        String normalized = message.trim();

        this.message = normalized.length()
                > MAX_MESSAGE_LENGTH
                ? normalized.substring(0, MAX_MESSAGE_LENGTH)
                : normalized;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
