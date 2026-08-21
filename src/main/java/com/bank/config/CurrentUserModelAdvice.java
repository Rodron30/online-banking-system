package com.bank.config;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.bank.model.User;
import com.bank.service.UserService;

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
