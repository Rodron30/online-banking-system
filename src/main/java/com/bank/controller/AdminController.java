package com.bank.controller;

import com.bank.model.Account;
import com.bank.model.AccountStatus;
import com.bank.model.NotificationType;
import com.bank.service.AccountService;
import com.bank.service.NotificationService;
import com.bank.service.TransactionService;
import com.bank.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final NotificationService notificationService;

    public AdminController(
            UserService userService,
            AccountService accountService,
            TransactionService transactionService,
            NotificationService notificationService) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String adminDashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("accounts", accountService.getAllAccounts());
        model.addAttribute("transactions", transactionService.getAllTransactions());
        return "admin-dashboard";
    }

    @PostMapping("/accounts/{id}/freeze")
    public String freezeAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Account account = accountService.getById(id);
            accountService.setStatus(id, AccountStatus.FROZEN);

            notificationService.notify(
                    account.getOwner(),
                    NotificationType.ACCOUNT_FROZEN,
                    "Your account " + account.getAccountNumber()
                            + " has been frozen by the administrator."
            );

            redirectAttributes.addFlashAttribute("success", "Na-freeze ang account.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/accounts/{id}/activate")
    public String activateAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Account account = accountService.getById(id);
            accountService.setStatus(id, AccountStatus.ACTIVE);

            notificationService.notify(
                    account.getOwner(),
                    NotificationType.ACCOUNT_ACTIVATED,
                    "Your account " + account.getAccountNumber()
                            + " has been reactivated by the administrator."
            );

            redirectAttributes.addFlashAttribute("success", "Na-activate ang account.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    /**
     * Soft-delete an account.
     *
     * The account disappears from the dashboard and can no
     * longer be used, but its data (including transaction
     * history) stays in the database for audit purposes.
     */
    @PostMapping("/accounts/{id}/delete")
    public String deleteAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Account account = accountService.getById(id);

            /*
             * Notify before deleting: after deletion the
             * account can no longer be resolved for the
             * notification bell query, but the message
             * itself remains a useful audit record.
             */
            notificationService.notify(
                    account.getOwner(),
                    NotificationType.ACCOUNT_DELETED,
                    "Your account " + account.getAccountNumber()
                            + " has been deleted by the administrator."
            );

            accountService.deleteAccount(id);

            redirectAttributes.addFlashAttribute("success", "Na-delete ang account.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }
}
