package com.bank.controller;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bank.model.Notification;
import com.bank.model.User;
import com.bank.service.NotificationService;
import com.bank.service.UserService;

/**
 * JSON API used by the notification bell dropdown
 * (see fragments/topbar.html) to load and manage
 * notifications for the currently authenticated user.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(
            NotificationService notificationService,
            UserService userService) {

        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public List<Map<String, Object>> list(Authentication auth) {

        User user = requireUser(auth);

        return notificationService
                .getRecentForUser(user)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication auth) {

        User user = requireUser(auth);

        Map<String, Long> body = new LinkedHashMap<>();
        body.put("count", notificationService.getUnreadCount(user));
        return body;
    }

    @PostMapping("/{id}/read")
    public void markAsRead(
            @PathVariable Long id,
            Authentication auth) {

        User user = requireUser(auth);
        notificationService.markAsRead(id, user);
    }

    @PostMapping("/read-all")
    public void markAllAsRead(Authentication auth) {

        User user = requireUser(auth);
        notificationService.markAllAsRead(user);
    }

    private User requireUser(Authentication auth) {

        if (auth == null
                || auth.getName() == null
                || !auth.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED
            );
        }

        return userService.getByUsername(auth.getName());
    }

    private Map<String, Object> toMap(Notification n) {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", n.getId());
        map.put("type", n.getType().name());
        map.put("message", n.getMessage());
        map.put("isRead", n.isRead());
        map.put(
                "createdAt",
                n.getCreatedAt() != null
                        ? n.getCreatedAt().format(FORMATTER)
                        : ""
        );
        return map;
    }
}
