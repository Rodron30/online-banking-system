package com.bank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bank.model.Notification;
import com.bank.model.User;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    /**
     * Latest notifications for a user, newest first.
     * Limited to the 10 most recent via Spring Data's
     * "Top10" keyword.
     */
    List<Notification> findTop10ByUserOrderByCreatedAtDesc(
            User user
    );

    /**
     * Count of unread notifications for the bell badge.
     */
    long countByUserAndIsReadFalse(User user);

    /**
     * Mark a single notification as read, scoped to
     * its owner so one user cannot mark another
     * user's notification as read.
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE Notification n
            SET n.isRead = true
            WHERE n.id = :id
            AND n.user = :user
            """)
    int markAsRead(
            @Param("id") Long id,
            @Param("user") User user
    );

    /**
     * Mark all of a user's notifications as read.
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE Notification n
            SET n.isRead = true
            WHERE n.user = :user
            AND n.isRead = false
            """)
    int markAllAsRead(@Param("user") User user);
}
