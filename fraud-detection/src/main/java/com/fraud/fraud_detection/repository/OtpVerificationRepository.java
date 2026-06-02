package com.fraud.fraud_detection.repository;

import com.fraud.fraud_detection.model.OtpVerification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpVerificationRepository
        extends JpaRepository<
        OtpVerification,
        Long
        > {

    OtpVerification findByTransactionId(
            Long transactionId
    );
}