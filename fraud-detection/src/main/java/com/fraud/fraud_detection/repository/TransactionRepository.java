package com.fraud.fraud_detection.repository;

import com.fraud.fraud_detection.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}