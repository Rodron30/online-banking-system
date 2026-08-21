package com.bank.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.model.Account;
import com.bank.model.User;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find a non-deleted account owned by a specific user.
     *
     * Excludes soft-deleted accounts so a deleted account
     * cannot be used again (e.g. to register a new account
     * for the same user, or to log a transaction).
     */
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.owner = :owner
            AND a.deletedAt IS NULL
            """)
    Optional<Account> findByOwner(@Param("owner") User owner);

    /**
     * Find an account for a user INCLUDING soft-deleted accounts.
     *
     * This is intentionally separate from findByOwner() because
     * the application normally works only with active accounts,
     * while startup initialization must also see an existing
     * soft-deleted account to avoid violating the unique user_id
     * constraint when the application restarts.
     */
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.owner = :owner
            """)
    Optional<Account> findByOwnerIncludingDeleted(
            @Param("owner") User owner
    );

    /**
     * Find a non-deleted account using the owner's username.
     */
    @Query("""
            SELECT a
            FROM Account a
            JOIN a.owner o
            WHERE o.username = :username
            AND a.deletedAt IS NULL
            """)
    Optional<Account> findByOwnerUsername(
            @Param("username") String username
    );

    /**
     * Find a non-deleted account using its account number.
     */
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.accountNumber = :accountNumber
            AND a.deletedAt IS NULL
            """)
    Optional<Account> findByAccountNumber(
            @Param("accountNumber") String accountNumber
    );

    /**
     * Check whether an account number already exists.
     *
     * Deleted accounts still count here, since account
     * numbers must remain globally unique.
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Find all NON-DELETED accounts together with their owners,
     * for the Admin Dashboard.
     *
     * JOIN FETCH ensures that owner is loaded
     * before the Thymeleaf admin dashboard accesses it.
     */
    @Query("""
            SELECT a
            FROM Account a
            JOIN FETCH a.owner
            WHERE a.deletedAt IS NULL
            ORDER BY a.id DESC
            """)
    List<Account> findAllWithOwner();

    /**
     * Find an account by account number
     * and lock the database row for update.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.accountNumber = :accountNumber
            """)
    Optional<Account> findByAccountNumberForUpdate(
            @Param("accountNumber") String accountNumber
    );

    /**
     * Find an account by ID
     * and lock the database row for update.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.id = :id
            """)
    Optional<Account> findByIdForUpdate(
            @Param("id") Long id
    );
}