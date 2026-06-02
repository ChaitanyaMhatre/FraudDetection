package com.fraud.fraud_detection.service;
import com.fraud.fraud_detection.dto.FraudAnalysisRequest;

import com.fraud.fraud_detection.model.Transaction;
import com.fraud.fraud_detection.repository.TransactionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class TransactionService {

    @Autowired
    private OtpService otpService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AiFraudService aiFraudService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Transaction processTransaction(
            Transaction transaction,
            FraudAnalysisRequest request
    ) {

        // ✅ Timestamp
        transaction.setTimestamp(
                LocalDateTime.now()
        );

        // 🤖 AI Analysis
        Map<String, Object> aiResponse =
                aiFraudService.analyzeTransaction(
                        request
                );

        Integer prediction =
                (Integer) aiResponse.get(
                        "prediction"
                );

        Double confidence =
                Double.valueOf(
                        aiResponse.get(
                                "confidence"
                        ).toString()
                );

        Object reasons =
                aiResponse.get("reasons");

        // 🟢 SAFE
        if (confidence < 40) {

            transaction.setStatus(
                    "SUCCESS"
            );

            transaction =
                    transactionRepository.save(
                            transaction
                    );

            messagingTemplate.convertAndSend(
                    "/topic/fraud-alerts",

                    "✅ Safe Transaction\n\n"

                            + "Amount: ₹"
                            + transaction.getAmount()

                            + "\n\nConfidence: "
                            + confidence + "%"
            );
        }

        // ⚠️ SUSPICIOUS
        else if (confidence >= 40
                && confidence < 90) {

            transaction.setStatus(
                    "SUSPICIOUS"
            );

            transaction =
                    transactionRepository.save(
                            transaction
                    );

            messagingTemplate.convertAndSend(
                    "/topic/fraud-alerts",

                    "⚠️ Suspicious Transaction\n\n"

                            + "Amount: ₹"
                            + transaction.getAmount()

                            + "\n\nConfidence: "
                            + confidence + "%"

                            + "\n\nReasons: "
                            + reasons

                            + "\n\nAction Required:"
                            + " Verify User"
            );
        }

        // 🚨 BLOCKED
        else {

            boolean strongFraudSignals =

                    request.getDeviceChange() == 1
                            ||
                            request.getLocationRisk() == 1
                            ||
                            request.getTransactionFrequency() > 10;

            // 🚨 Truly dangerous
            if (strongFraudSignals) {

                transaction.setStatus(
                        "BLOCKED"
                );

                transaction =
                        transactionRepository.save(
                                transaction
                        );

                messagingTemplate.convertAndSend(
                        "/topic/fraud-alerts",

                        "🚨 AI Fraud Detected!\n\n"

                                + "Amount: ₹"
                                + transaction.getAmount()

                                + "\n\nConfidence: "
                                + confidence + "%"

                                + "\n\nReasons: "
                                + reasons
                );

            }

            // ⚠️ High amount but not enough fraud evidence
            else {

                transaction.setStatus(
                        "SUSPICIOUS"
                );

                transaction =
                        transactionRepository.save(
                                transaction
                        );
                String otp = otpService.generateOtp(
                        transaction.getId(),
                        transaction.getSenderId()
                );

                System.out.println(
                        "🔐 OTP FOR TRANSACTION "
                                + transaction.getId()
                                + ": "
                                + otp
                );



                messagingTemplate.convertAndSend(
                        "/topic/fraud-alerts",

                        "⚠️ Suspicious High-Value Transaction\n\n"

                                + "Amount: ₹"
                                + transaction.getAmount()

                                + "\n\nConfidence: "
                                + confidence + "%"

                                + "\n\nReasons: "
                                + reasons
                );
            }
        }

        return transaction;
    }
}