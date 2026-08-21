package com.bank.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.model.Notification;
import com.bank.model.NotificationType;
import com.bank.model.User;
import com.bank.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {

        this.notificationRepository = notificationRepository;
    }

    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Create a notification for a user.
     *
     * Called internally by other services (TransactionService,
     * UserService, AdminController) right after the real event
     * happens. Never called directly from the browser.
     */
    @Transactional
    public void notify(
            User user,
            NotificationType type,
            String message) {

        if (user == null) {

            throw new IllegalArgumentException(
                    "Cannot create a notification without a user."
            );
        }

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);

        notificationRepository.save(notification);
    }

    // =========================================================
    // READ
    // =========================================================

    @Transactional(readOnly = true)
    public List<Notification> getRecentForUser(User user) {

        return notificationRepository
                .findTop10ByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {

        return notificationRepository
                .countByUserAndIsReadFalse(user);
    }

    // =========================================================
    // MARK AS READ
    // =========================================================

    @Transactional
    public void markAsRead(Long notificationId, User user) {

        notificationRepository.markAsRead(
                notificationId,
                user
        );
    }

    @Transactional
    public void markAllAsRead(User user) {

        notificationRepository.markAllAsRead(user);
    }
}
