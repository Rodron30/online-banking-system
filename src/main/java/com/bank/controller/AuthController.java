package com.bank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bank.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

  
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
  
    @PostMapping("/register")
    public String register(
            @RequestParam(name = "fullName") String fullName,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "email") String email,
            @RequestParam(name = "password") String password,
            @RequestParam(name = "confirmPassword") String confirmPassword,
            Model model) {

        /*
         * Check password confirmation first.
         */
        if (password == null
                || confirmPassword == null
                || !password.equals(confirmPassword)) {

            model.addAttribute(
                    "error",
                    "Hindi magkatugma ang password at confirm password."
            );

            return "register";
        }

        try {

            /*
             * UserService handles:
             *
             * - input validation
             * - username uniqueness
             * - email uniqueness
             * - password validation
             * - BCrypt password hashing
             * - USER role assignment
             * - automatic bank account creation
             */
            userService.register(
                    fullName,
                    username,
                    email,
                    password
            );

            /*
             * Registration successful.
             *
             * Login page will display:
             * "Registration successful!"
             */
            return "redirect:/login?registered";

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            /*
             * Display validation/business error
             * back on the registration page.
             */
            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "register";
        }
    }
}