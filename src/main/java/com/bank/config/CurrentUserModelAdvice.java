package com.bank.config;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.bank.model.User;
import com.bank.service.UserService;

/**
 * Makes the currently authenticated application User available
 * to all Thymeleaf views.
 *
 * This is required by the shared navbar because secondary pages
 * (Transfer, Deposit/Withdraw, History, Admin) do not otherwise
 * need to load the User object themselves.
 */
@ControllerAdvice
public class CurrentUserModelAdvice {

    private final UserService userService;

    public CurrentUserModelAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("user")
    public User currentUser(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }

        return userService.getByUsername(authentication.getName());
    }
}
