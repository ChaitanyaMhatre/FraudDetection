package com.fraud.fraud_detection.repository;

import com.fraud.fraud_detection.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderIdOrReceiverIdOrderByIdDesc(Long senderId, Long receiverId);

    // Count transaction frequency in a time window (e.g. last 24 hours)
    long countBySenderIdAndTimestampAfter(Long senderId, LocalDateTime timestamp);

    // Calculate average amount of previous successful transactions for a user
    @Query("SELECT COALESCE(AVG(t.amount), 0.0) FROM Transaction t WHERE t.senderId = :senderId AND t.status = 'SUCCESS'")
    double getAverageTransactionAmountForUser(@Param("senderId") Long senderId);
}