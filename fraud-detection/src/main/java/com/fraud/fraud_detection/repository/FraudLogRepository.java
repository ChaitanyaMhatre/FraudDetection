package com.fraud.fraud_detection.repository;

import com.fraud.fraud_detection.model.FraudLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudLogRepository extends JpaRepository<FraudLog, Long> {
}