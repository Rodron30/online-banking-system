package com.bank.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bank.model.Account;
import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.service.TransactionService;
import com.bank.service.UserService;

@Controller
public class DashboardController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final UserService userService;

    public DashboardController(
            AccountService accountService,
            TransactionService transactionService,
            UserService userService) {

        this.accountService = accountService;
        this.transactionService = transactionService;
        this.userService = userService;
    }

    /**
     * Display the dashboard of the currently authenticated user.
     *
     * Only the authenticated user's account and transactions
     * are loaded.
     */
    @GetMapping("/dashboard")
    public String dashboard(
            Authentication auth,
            Model model) {

        if (!isAuthenticated(auth)) {
            return "redirect:/login";
        }

        String username = auth.getName();

        User user =
                userService.getByUsername(username);

        Account account =
                accountService.getAccountForUsername(username);

        model.addAttribute(
                "user",
                user
        );

        model.addAttribute(
                "account",
                account
        );

        // Server-side date/time for the dashboard header.
        model.addAttribute(
                "now",
                LocalDateTime.now()
        );

        /*
         * Display only the five most recent transactions.
         */
        model.addAttribute(
                "recentTransactions",
                transactionService
                        .getHistory(account)
                        .stream()
                        .limit(5)
                        .toList()
        );

        return "dashboard";
    }
  
    /**
     * Display the deposit and withdrawal page.
     */
    @GetMapping("/deposit-withdraw")
    public String depositWithdrawPage(
            Authentication auth) {

        if (!isAuthenticated(auth)) {
            return "redirect:/login";
        }

        return "deposit-withdraw";
    }

    /**
     * Deposit money into the authenticated user's account.
     */
    @PostMapping("/deposit")
    public String deposit(
            Authentication auth,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {

        if (!isAuthenticated(auth)) {
            return "redirect:/login";
        }

        try {

            Account account =
                    accountService.getAccountForUsername(
                            auth.getName()
                    );

            /*
             * TransactionService performs the actual
             * amount validation and account locking.
             */
            transactionService.deposit(
                    account,
                    amount,
                    description
            );

            BigDecimal displayAmount =
                    normalizeDisplayAmount(amount);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Deposit successful: ₱"
                            + displayAmount
            );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/dashboard";
    }

    /**
     * Withdraw money from the authenticated user's account.
     */
    @PostMapping("/withdraw")
    public String withdraw(
            Authentication auth,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {

        if (!isAuthenticated(auth)) {
            return "redirect:/login";
        }

        try {

            Account account =
                    accountService.getAccountForUsername(
                            auth.getName()
                    );

            /*
             * TransactionService validates:
             *
             * - amount
             * - account status
             * - available balance
             */
            transactionService.withdraw(
                    account,
                    amount,
                    description
            );

            BigDecimal displayAmount =
                    normalizeDisplayAmount(amount);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Withdrawal successful: ₱"
                            + displayAmount
            );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/dashboard";
    }

    /**
     * Display the authenticated user's profile page.
     */
    @GetMapping("/profile")
    public String profilePage(
            Authentication auth) {

        if (!isAuthenticated(auth)) {
            return "redirect:/login";
        }

        return "profile";
    }

    /**
     * Display the change PIN page.
     */
    @GetMapping("/change-pin")
    public String changePinPage(
            Authentication auth) {

        if (!isAuthenticated(auth)) {
            return "redirect:/login";
        }

        return "change-pin";
    }

    /**
     * Change the authenticated user's ATM PIN.
     */
    @PostMapping("/change-pin")
    public String changePin(
            Authentication auth,
            @RequestParam String currentPin,
            @RequestParam String newPin,
            @RequestParam String confirmPin,
            RedirectAttributes redirectAttributes) {

        if (!isAuthenticated(auth)) {
            return "redirect:/login";
        }

        try {

            userService.changePin(
                    auth.getName(),
                    currentPin,
                    newPin,
                    confirmPin
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Your PIN has been changed successfully."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/change-pin";
    }

    /**
     * Display the transfer page.
     */
    @GetMapping("/transfer")
    public String transferPage(
            Authentication auth) {

        if (!isAuthenticated(auth)) {
            return "redirect:/login";
        }

        return "transfer";
    }

    /**
     * Transfer money from the authenticated user's account
     * to another bank account.
     */
    @PostMapping("/transfer")
    public String transfer(
            Authentication auth,
            @RequestParam String toAccountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!isAuthenticated(auth)) {
            return "redirect:/login";
        }

        try {

            /*
             * Always obtain the source account from
             * the authenticated username.
             *
             * We do NOT accept a source account number
             * from the browser.
             */
            Account sourceAccount =
                    accountService.getAccountForUsername(
                            auth.getName()
                    );

            /*
             * TransactionService handles:
             *
             * - amount validation
             * - destination validation
             * - self-transfer prevention
             * - account status validation
             * - balance validation
             * - database row locking
             * - deadlock prevention
             * - TRANSFER_OUT creation
             * - TRANSFER_IN creation
             */
            transactionService.transfer(
                    sourceAccount,
                    toAccountNumber,
                    amount,
                    description
            );

            BigDecimal displayAmount =
                    normalizeDisplayAmount(amount);

            String destination =
                    toAccountNumber == null
                            ? ""
                            : toAccountNumber.trim();

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Transfer successful: ₱"
                            + displayAmount
                            + " to "
                            + destination
            );

            return "redirect:/dashboard";

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            /*
             * Return to the transfer page so the user
             * can correct the input.
             */
            return "transfer";
        }
    }

    /**
     * Check whether a valid authenticated user exists.
     */
    private boolean isAuthenticated(
            Authentication auth) {

        return auth != null
                && auth.isAuthenticated()
                && auth.getName() != null
                && !auth.getName().isBlank();
    }

    /**
     * Normalize a successfully submitted amount
     * for displaying in a success message.
     *
     * TransactionService performs the real validation.
     */
    private BigDecimal normalizeDisplayAmount(
            BigDecimal amount) {

        if (amount == null) {
            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.UNNECESSARY
            );
        }

        return amount.setScale(
                2,
                RoundingMode.UNNECESSARY
        );
    }
}