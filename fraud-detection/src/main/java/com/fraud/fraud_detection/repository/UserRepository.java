package com.fraud.fraud_detection.repository;

import com.fraud.fraud_detection.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository
        extends JpaRepository<
        User,
        Long
        > {
}