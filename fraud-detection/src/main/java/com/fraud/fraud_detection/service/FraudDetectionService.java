package com.fraud.fraud_detection.service;

import org.springframework.stereotype.Service;

@Service
public class FraudDetectionService {

    public int calculateRiskScore(Double amount, int recentTransactions) {
        int score = 0;

        // Rule 1: High amount
        if (amount > 50000) {
            score += 50;
        }

        // Rule 2: Too many transactions quickly
        if (recentTransactions > 3) {
            score += 30;
        }

        // Rule 3: Medium amount
        if (amount > 20000) {
            score += 20;
        }
        System.out.println("Amount: " + amount + " Score: " + score);
        return score;
    }

    public String getDecision(int score) {
        if (score >= 70) {
            return "BLOCKED";
        } else if (score >= 50) {
            return "SUSPICIOUS";
        } else {
            return "SAFE";
        }
    }
}