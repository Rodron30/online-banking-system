package com.bank.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bank.model.Account;
import com.bank.service.AccountService;
import com.bank.service.TransactionService;

@Controller
public class TransactionController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public TransactionController(
            AccountService accountService,
            TransactionService transactionService) {

        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    /**
     * Display the transaction history of the
     * currently authenticated user's account.
     */
    @GetMapping("/transactions")
    public String transactions(
            Authentication auth,
            Model model) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = auth.getName();

        /*
         * Get ONLY the bank account belonging
         * to the authenticated user.
         */
        Account account =
                accountService.getAccountForUsername(username);

        /*
         * Load transaction history for that account.
         */
        model.addAttribute("account", account);

        model.addAttribute(
                "transactions",
                transactionService.getHistory(account)
        );

        return "transactions";
    }
}