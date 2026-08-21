package com.bank;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.service.TransactionService;
import com.bank.service.UserService;

public class ConsoleTest {

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final PasswordEncoder passwordEncoder;

    private final Scanner scanner = new Scanner(System.in);

    public ConsoleTest(
            UserService userService,
            AccountService accountService,
            TransactionService transactionService,
            PasswordEncoder passwordEncoder) {

        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // MAIN MENU
    // =========================================================

    public void showMainMenu() {

        while (true) {

            System.out.println();
            System.out.println("================================");
            System.out.println("       RODRON BANK SYSTEM");
            System.out.println("================================");
            System.out.println("1. Login");
            System.out.println("2. Create Account");
            System.out.println("3. Exit");
            System.out.println();

            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    login();
                    break;

                case "2":
                    createAccount();
                    break;

                case "3":
                    System.out.println();
                    System.out.println(
                            "Thank you for using Rodron Bank."
                    );

                    scanner.close();
                    return;

                default:
                    System.out.println();
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }

    // =========================================================
    // CREATE ACCOUNT
    // =========================================================

    private void createAccount() {

        System.out.println();
        System.out.println("================================");
        System.out.println("        CREATE ACCOUNT");
        System.out.println("================================");

        try {

            System.out.print("Full Name: ");
            String fullName = scanner.nextLine();

            System.out.print("Username: ");
            String username = scanner.nextLine();

            System.out.print("Email: ");
            String email = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            User user = userService.register(
                    fullName,
                    username,
                    email,
                    password
            );

            /*
             * AccountService method:
             * getAccountForUsername()
             */
            Account account =
                    accountService.getAccountForUsername(
                            user.getUsername()
                    );

            System.out.println();
            System.out.println("Account created successfully.");
            System.out.println("--------------------------------");
            System.out.println("Name: "
                    + user.getFullName());
            System.out.println("Username: "
                    + user.getUsername());
            System.out.println("Email: "
                    + user.getEmail());
            System.out.println("Account Number: "
                    + account.getAccountNumber());
            System.out.println("Balance: ₱"
                    + account.getBalance());
            System.out.println("--------------------------------");

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Account creation failed: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private void login() {

        System.out.println();
        System.out.println("================================");
        System.out.println("             LOGIN");
        System.out.println("================================");

        try {

            System.out.print("Username: ");
            String username =
                    scanner.nextLine().trim();

            System.out.print("Password: ");
            String password =
                    scanner.nextLine();

            User user =
                    userService.getByUsername(username);

            if (!passwordEncoder.matches(
                    password,
                    user.getPassword())) {

                System.out.println();
                System.out.println(
                        "Invalid username or password."
                );

                return;
            }

            Account account =
                    accountService.getAccountForUsername(
                            user.getUsername()
                    );

            System.out.println();
            System.out.println("Login successful.");
            System.out.println(
                    "Welcome, "
                            + user.getFullName()
                            + "!"
            );

            bankingMenu(user, account);

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Login failed: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // BANKING MENU
    // =========================================================

    private void bankingMenu(
            User user,
            Account account) {

        while (true) {

            System.out.println();
            System.out.println("================================");
            System.out.println("          BANKING MENU");
            System.out.println("================================");
            System.out.println(
                    "Account: "
                            + account.getAccountNumber()
            );
            System.out.println(
                    "Welcome: "
                            + user.getFullName()
            );
            System.out.println("--------------------------------");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Transaction History");
            System.out.println("6. Logout");
            System.out.println();

            System.out.print("Enter choice: ");

            String choice =
                    scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    checkBalance(account);
                    break;

                case "2":
                    deposit(account);
                    break;

                case "3":
                    withdraw(account);
                    break;

                case "4":
                    transfer(account);
                    break;

                case "5":
                    showHistory(account);
                    break;

                case "6":
                    System.out.println();
                    System.out.println(
                            "Logged out successfully."
                    );
                    return;

                default:
                    System.out.println();
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }

    // =========================================================
    // CHECK BALANCE
    // =========================================================

    private void checkBalance(Account account) {

        try {

            Account currentAccount =
                    accountService.getByAccountNumber(
                            account.getAccountNumber()
                    );

            System.out.println();
            System.out.println("================================");
            System.out.println("          ACCOUNT BALANCE");
            System.out.println("================================");

            System.out.println(
                    "Account Number: "
                            + currentAccount.getAccountNumber()
            );

            System.out.println(
                    "Balance: ₱"
                            + currentAccount.getBalance()
            );

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Unable to retrieve balance: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // DEPOSIT
    // =========================================================

    private void deposit(Account account) {

        try {

            System.out.print(
                    "Amount to deposit: ₱"
            );

            BigDecimal amount =
                    new BigDecimal(
                            scanner.nextLine().trim()
                    );

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {

                System.out.println();
                System.out.println(
                        "Amount must be greater than zero."
                );

                return;
            }

            System.out.print("Description: ");

            String description =
                    scanner.nextLine().trim();

            transactionService.deposit(
                    account,
                    amount,
                    description
            );

            System.out.println();
            System.out.println(
                    "Deposit successful."
            );

        } catch (NumberFormatException e) {

            System.out.println();
            System.out.println(
                    "Invalid amount."
            );

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Deposit failed: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // WITHDRAW
    // =========================================================

    private void withdraw(Account account) {

        try {

            System.out.print(
                    "Amount to withdraw: ₱"
            );

            BigDecimal amount =
                    new BigDecimal(
                            scanner.nextLine().trim()
                    );

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {

                System.out.println();
                System.out.println(
                        "Amount must be greater than zero."
                );

                return;
            }

            System.out.print("Description: ");

            String description =
                    scanner.nextLine().trim();

            transactionService.withdraw(
                    account,
                    amount,
                    description
            );

            System.out.println();
            System.out.println(
                    "Withdrawal successful."
            );

        } catch (NumberFormatException e) {

            System.out.println();
            System.out.println(
                    "Invalid amount."
            );

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Withdrawal failed: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // TRANSFER
    // =========================================================

    private void transfer(Account account) {

        try {

            System.out.print(
                    "Destination Account Number: "
            );

            String destination =
                    scanner.nextLine().trim();

            if (!destination.matches("\\d{16}")) {

                System.out.println();
                System.out.println(
                        "Account number must contain exactly 16 digits."
                );

                return;
            }

            System.out.print(
                    "Amount to transfer: ₱"
            );

            BigDecimal amount =
                    new BigDecimal(
                            scanner.nextLine().trim()
                    );

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {

                System.out.println();
                System.out.println(
                        "Amount must be greater than zero."
                );

                return;
            }

            System.out.print("Description: ");

            String description =
                    scanner.nextLine().trim();

            transactionService.transfer(
                    account,
                    destination,
                    amount,
                    description
            );

            System.out.println();
            System.out.println(
                    "Transfer successful."
            );

        } catch (NumberFormatException e) {

            System.out.println();
            System.out.println(
                    "Invalid amount."
            );

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Transfer failed: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // TRANSACTION HISTORY
    // =========================================================

    private void showHistory(Account account) {

        try {

            List<Transaction> transactions =
                    transactionService.getHistory(account);

            System.out.println();
            System.out.println("================================");
            System.out.println("      TRANSACTION HISTORY");
            System.out.println("================================");

            if (transactions == null
                    || transactions.isEmpty()) {

                System.out.println(
                        "No transactions found."
                );

                return;
            }

            for (Transaction transaction : transactions) {

                System.out.println("--------------------------------");

                System.out.println(
                        "Type: "
                                + transaction.getType()
                );

                System.out.println(
                        "Amount: ₱"
                                + transaction.getAmount()
                );

                System.out.println(
                        "Balance After: ₱"
                                + transaction.getBalanceAfter()
                );

                System.out.println(
                        "Description: "
                                + (
                                transaction.getDescription() != null
                                        ? transaction.getDescription()
                                        : "—"
                        )
                );

                if (transaction
                        .getCounterpartyAccountNumber() != null) {

                    System.out.println(
                            "Counterparty: "
                                    + transaction
                                    .getCounterpartyAccountNumber()
                    );
                }

                System.out.println(
                        "Timestamp: "
                                + transaction.getTimestamp()
                );
            }

            System.out.println("--------------------------------");

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Unable to retrieve history: "
                            + e.getMessage()
            );
        }
    }
}