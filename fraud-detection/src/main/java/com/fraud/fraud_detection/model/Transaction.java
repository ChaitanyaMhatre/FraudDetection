package com.fraud.fraud_detection.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;
    private Long receiverId;

    private Double amount;

    private String status; // SUCCESS / FAILED / BLOCKED

    private LocalDateTime timestamp;

    private Double latitude;
    private Double longitude;
}