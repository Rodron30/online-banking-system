package com.bank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bank.model.Account;
import com.bank.model.Transaction;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    /**
     * Get transaction history for a specific account,
     * newest transaction first.
     *
     * JOIN FETCH loads the account together with
     * each transaction to avoid LazyInitializationException.
     */
    @Query("""
            SELECT t
            FROM Transaction t
            JOIN FETCH t.account
            WHERE t.account = :account
            ORDER BY t.timestamp DESC
            """)
    List<Transaction> findByAccountOrderByTimestampDesc(
            Account account
    );

    /**
     * Get all transactions in the system,
     * newest transaction first.
     *
     * JOIN FETCH loads the related account and owner
     * so the Admin Dashboard can safely display
     * account information.
     */
    @Query("""
            SELECT t
            FROM Transaction t
            JOIN FETCH t.account
            JOIN FETCH t.account.owner
            ORDER BY t.timestamp DESC
            """)
    List<Transaction> findAllByOrderByTimestampDesc();
}