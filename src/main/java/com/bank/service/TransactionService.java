package com.bank.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.model.Account;
import com.bank.model.AccountStatus;
import com.bank.model.NotificationType;
import com.bank.model.Transaction;
import com.bank.model.TransactionType;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;

@Service
public class TransactionService {

    private static final int MONEY_SCALE = 2;
    private static final int MAX_DESCRIPTION_LENGTH = 255;
    private static final int ACCOUNT_NUMBER_LENGTH = 16;

    private static final BigDecimal MINIMUM_AMOUNT =
            new BigDecimal("0.01");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    public TransactionService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            NotificationService notificationService) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
    }

    // =========================================================
    // DEPOSIT
    // =========================================================

    @Transactional
    public void deposit(
            Account account,
            BigDecimal amount,
            String description) {

        validateAccount(account);

        BigDecimal validAmount = validateAmount(amount);

        Account lockedAccount =
                accountRepository
                        .findByIdForUpdate(account.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found."
                                ));

        validateActive(lockedAccount);

        BigDecimal currentBalance =
                getSafeBalance(lockedAccount);

        BigDecimal newBalance =
                currentBalance
                        .add(validAmount)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.UNNECESSARY
                        );

        // Update balance
        lockedAccount.setBalance(newBalance);
        accountRepository.save(lockedAccount);

        // Create transaction
        Transaction transaction = new Transaction();

        transaction.setAccount(lockedAccount);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(validAmount);
        transaction.setBalanceAfter(newBalance);

        transaction.setDescription(
                normalizeDescription(
                        description,
                        "Deposit"
                )
        );

        transactionRepository.save(transaction);

        // Create notification AFTER successful transaction
        notificationService.notify(
                lockedAccount.getOwner(),
                NotificationType.DEPOSIT,
                "Deposit successful: ₱"
                        + validAmount
                        + ". New balance: ₱"
                        + newBalance
        );
    }

    // =========================================================
    // WITHDRAW
    // =========================================================

    @Transactional
    public void withdraw(
            Account account,
            BigDecimal amount,
            String description) {

        validateAccount(account);

        BigDecimal validAmount = validateAmount(amount);

        Account lockedAccount =
                accountRepository
                        .findByIdForUpdate(account.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found."
                                ));

        validateActive(lockedAccount);

        BigDecimal currentBalance =
                getSafeBalance(lockedAccount);

        if (currentBalance.compareTo(validAmount) < 0) {

            throw new IllegalStateException(
                    "Insufficient balance for this withdrawal."
            );
        }

        BigDecimal newBalance =
                currentBalance
                        .subtract(validAmount)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.UNNECESSARY
                        );

        // Update balance
        lockedAccount.setBalance(newBalance);
        accountRepository.save(lockedAccount);

        // Create transaction
        Transaction transaction = new Transaction();

        transaction.setAccount(lockedAccount);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setAmount(validAmount);
        transaction.setBalanceAfter(newBalance);

        transaction.setDescription(
                normalizeDescription(
                        description,
                        "Withdrawal"
                )
        );

        transactionRepository.save(transaction);

        // Create notification AFTER successful transaction
        notificationService.notify(
                lockedAccount.getOwner(),
                NotificationType.WITHDRAWAL,
                "Withdrawal successful: ₱"
                        + validAmount
                        + ". New balance: ₱"
                        + newBalance
        );
    }

    // =========================================================
    // TRANSFER
    // =========================================================

    @Transactional
    public void transfer(
            Account fromAccount,
            String toAccountNumber,
            BigDecimal amount,
            String description) {

        validateAccount(fromAccount);

        BigDecimal validAmount = validateAmount(amount);

        String destinationAccountNumber =
                normalizeAccountNumber(toAccountNumber);

        if (fromAccount.getAccountNumber() == null) {

            throw new IllegalStateException(
                    "Source account has no account number."
            );
        }

        if (fromAccount
                .getAccountNumber()
                .equals(destinationAccountNumber)) {

            throw new IllegalArgumentException(
                    "You cannot transfer money to your own account."
            );
        }

        Account destination =
                accountRepository
                        .findByAccountNumber(
                                destinationAccountNumber
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Destination account not found."
                                ));

        validateAccount(destination);

        Account firstLocked;
        Account secondLocked;

        // Lock accounts in ID order
        if (fromAccount
                .getId()
                .compareTo(destination.getId()) < 0) {

            firstLocked =
                    accountRepository
                            .findByIdForUpdate(
                                    fromAccount.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Source account not found."
                                    ));

            secondLocked =
                    accountRepository
                            .findByIdForUpdate(
                                    destination.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Destination account not found."
                                    ));

        } else {

            firstLocked =
                    accountRepository
                            .findByIdForUpdate(
                                    destination.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Destination account not found."
                                    ));

            secondLocked =
                    accountRepository
                            .findByIdForUpdate(
                                    fromAccount.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Source account not found."
                                    ));
        }

        Account lockedFromAccount;
        Account lockedToAccount;

        if (firstLocked
                .getId()
                .equals(fromAccount.getId())) {

            lockedFromAccount = firstLocked;
            lockedToAccount = secondLocked;

        } else {

            lockedFromAccount = secondLocked;
            lockedToAccount = firstLocked;
        }

        validateActive(lockedFromAccount);
        validateActive(lockedToAccount);

        BigDecimal fromBalance =
                getSafeBalance(lockedFromAccount);

        BigDecimal toBalance =
                getSafeBalance(lockedToAccount);

        if (fromBalance.compareTo(validAmount) < 0) {

            throw new IllegalStateException(
                    "Insufficient balance for this transfer."
            );
        }

        // =====================================================
        // DEBIT SENDER
        // =====================================================

        BigDecimal senderNewBalance =
                fromBalance
                        .subtract(validAmount)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.UNNECESSARY
                        );

        lockedFromAccount.setBalance(
                senderNewBalance
        );

        accountRepository.save(
                lockedFromAccount
        );

        Transaction transferOut =
                new Transaction();

        transferOut.setAccount(
                lockedFromAccount
        );

        transferOut.setType(
                TransactionType.TRANSFER_OUT
        );

        transferOut.setAmount(
                validAmount
        );

        transferOut.setBalanceAfter(
                senderNewBalance
        );

        transferOut.setCounterpartyAccountNumber(
                lockedToAccount.getAccountNumber()
        );

        transferOut.setDescription(
                normalizeDescription(
                        description,
                        "Transfer sent"
                )
        );

        transactionRepository.save(
                transferOut
        );

        // =====================================================
        // CREDIT RECEIVER
        // =====================================================

        BigDecimal receiverNewBalance =
                toBalance
                        .add(validAmount)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.UNNECESSARY
                        );

        lockedToAccount.setBalance(
                receiverNewBalance
        );

        accountRepository.save(
                lockedToAccount
        );

        Transaction transferIn =
                new Transaction();

        transferIn.setAccount(
                lockedToAccount
        );

        transferIn.setType(
                TransactionType.TRANSFER_IN
        );

        transferIn.setAmount(
                validAmount
        );

        transferIn.setBalanceAfter(
                receiverNewBalance
        );

        transferIn.setCounterpartyAccountNumber(
                lockedFromAccount.getAccountNumber()
        );

        transferIn.setDescription(
                normalizeDescription(
                        description,
                        "Transfer received"
                )
        );

        transactionRepository.save(
                transferIn
        );

        // =====================================================
        // NOTIFICATIONS
        // =====================================================

        notificationService.notify(
                lockedFromAccount.getOwner(),
                NotificationType.TRANSFER_SENT,
                "You sent ₱"
                        + validAmount
                        + " to account "
                        + lockedToAccount.getAccountNumber()
        );

        notificationService.notify(
                lockedToAccount.getOwner(),
                NotificationType.TRANSFER_RECEIVED,
                "You received ₱"
                        + validAmount
                        + " from account "
                        + lockedFromAccount.getAccountNumber()
        );
    }

    // =========================================================
    // TRANSACTION HISTORY
    // =========================================================

    @Transactional(readOnly = true)
    public List<Transaction> getHistory(
            Account account) {

        validateAccount(account);

        return transactionRepository
                .findByAccountOrderByTimestampDesc(
                        account
                );
    }

    // =========================================================
    // GET ALL TRANSACTIONS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {

        return transactionRepository
                .findAllByOrderByTimestampDesc();
    }

    // =========================================================
    // AMOUNT VALIDATION
    // =========================================================

    private BigDecimal validateAmount(
            BigDecimal amount) {

        if (amount == null) {

            throw new IllegalArgumentException(
                    "Amount is required."
            );
        }

        if (amount.scale() > MONEY_SCALE) {

            throw new IllegalArgumentException(
                    "Amount may have up to "
                            + MONEY_SCALE
                            + " decimal places only."
            );
        }

        if (amount.compareTo(
                MINIMUM_AMOUNT) < 0) {

            throw new IllegalArgumentException(
                    "The minimum amount is ₱0.01."
            );
        }

        try {

            return amount.setScale(
                    MONEY_SCALE,
                    RoundingMode.UNNECESSARY
            );

        } catch (ArithmeticException e) {

            throw new IllegalArgumentException(
                    "Invalid monetary amount."
            );
        }
    }

    // =========================================================
    // ACCOUNT VALIDATION
    // =========================================================

    private void validateAccount(
            Account account) {

        if (account == null
                || account.getId() == null) {

            throw new IllegalArgumentException(
                    "Invalid account."
            );
        }
    }

    // =========================================================
    // ACCOUNT STATUS
    // =========================================================

    private void validateActive(
            Account account) {

        if (account.getStatus() == null) {

            throw new IllegalStateException(
                    "Account has no valid status."
            );
        }

        if (account.getStatus()
                == AccountStatus.FROZEN) {

            throw new IllegalStateException(
                    "This account is frozen. "
                            + "Please contact the administrator."
            );
        }

        if (account.getStatus()
                != AccountStatus.ACTIVE) {

            throw new IllegalStateException(
                    "This account is not active."
            );
        }
    }

    // =========================================================
    // SAFE BALANCE
    // =========================================================

    private BigDecimal getSafeBalance(
            Account account) {

        if (account.getBalance() == null) {

            throw new IllegalStateException(
                    "Account has no valid balance."
            );
        }

        if (account.getBalance()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalStateException(
                    "Invalid account balance."
            );
        }

        try {

            return account.getBalance()
                    .setScale(
                            MONEY_SCALE,
                            RoundingMode.UNNECESSARY
                    );

        } catch (ArithmeticException e) {

            throw new IllegalStateException(
                    "Invalid account balance."
            );
        }
    }

    // =========================================================
    // ACCOUNT NUMBER
    // =========================================================

    private String normalizeAccountNumber(
            String accountNumber) {

        if (accountNumber == null
                || accountNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Destination account number is required."
            );
        }

        String normalized =
                accountNumber.trim();

        if (!normalized.matches(
                "\\d{" + ACCOUNT_NUMBER_LENGTH + "}")) {

            throw new IllegalArgumentException(
                    "Account number must contain exactly "
                            + ACCOUNT_NUMBER_LENGTH
                            + " digits."
            );
        }

        return normalized;
    }

    // =========================================================
    // DESCRIPTION
    // =========================================================

    private String normalizeDescription(
            String description,
            String defaultDescription) {

        if (description == null
                || description.isBlank()) {

            return defaultDescription;
        }

        String cleaned =
                description.trim();

        if (cleaned.length()
                > MAX_DESCRIPTION_LENGTH) {

            throw new IllegalArgumentException(
                    "Description may contain up to "
                            + MAX_DESCRIPTION_LENGTH
                            + " characters only."
            );
        }

        return cleaned;
    }
}