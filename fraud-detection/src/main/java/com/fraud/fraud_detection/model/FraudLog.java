package com.fraud.fraud_detection.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fraud_logs")
public class FraudLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long transactionId;
    private int riskScore;
    private String reason;
    private String actionTaken;

    private LocalDateTime timestamp;
}
