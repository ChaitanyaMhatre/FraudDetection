package com.fraud.fraud_detection.service;

import com.fraud.fraud_detection.dto.FraudAnalysisRequest;
import com.fraud.fraud_detection.model.Transaction;
import com.fraud.fraud_detection.model.User;
import com.fraud.fraud_detection.repository.TransactionRepository;
import com.fraud.fraud_detection.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private OtpService otpService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AiFraudService aiFraudService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void updateBalances(Transaction transaction) {
        User sender = userRepository.findById(transaction.getSenderId()).orElse(null);
        User receiver = userRepository.findById(transaction.getReceiverId()).orElse(null);
        if (sender != null) {
            sender.setBalance(sender.getBalance() - transaction.getAmount());
            userRepository.save(sender);
        }
        if (receiver != null) {
            receiver.setBalance(receiver.getBalance() + transaction.getAmount());
            userRepository.save(receiver);
        }
    }

    public void assignCoordinatesIfEmpty(Transaction transaction, FraudAnalysisRequest request) {
        if (request.getLatitude() != 0.0 && request.getLongitude() != 0.0) {
            transaction.setLatitude(request.getLatitude());
            transaction.setLongitude(request.getLongitude());
        } else {
            double lat;
            double lng;
            if (request.getLocationRisk() == 1) {
                // Overseas high risk locations
                double[][] riskZones = {
                    {6.5244, 3.3792}, // Lagos, Nigeria
                    {44.4268, 26.1025}, // Bucharest, Romania
                    {55.7558, 37.6173} // Moscow, Russia
                };
                int index = (int) (Math.random() * riskZones.length);
                lat = riskZones[index][0] + (Math.random() - 0.5) * 0.5;
                lng = riskZones[index][1] + (Math.random() - 0.5) * 0.5;
            } else {
                // Major cities in India
                double[][] cities = {
                    {19.0760, 72.8777}, // Mumbai
                    {28.6139, 77.2090}, // Delhi
                    {12.9716, 77.5946}, // Bangalore
                    {22.5726, 88.3639}, // Kolkata
                    {13.0827, 80.2707}  // Chennai
                };
                int index = (int) (Math.random() * cities.length);
                lat = cities[index][0] + (Math.random() - 0.5) * 1.5;
                lng = cities[index][1] + (Math.random() - 0.5) * 1.5;
            }
            transaction.setLatitude(lat);
            transaction.setLongitude(lng);
        }
    }

    public Transaction processTransaction(
            Transaction transaction,
            FraudAnalysisRequest request
    ) {
        // 1. Validation
        if (transaction.getSenderId() == null || transaction.getReceiverId() == null) {
            throw new IllegalArgumentException("Sender and Recipient IDs are required.");
        }
        if (transaction.getSenderId().equals(transaction.getReceiverId())) {
            throw new IllegalArgumentException("Self-transfers are not allowed.");
        }
        if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        User sender = userRepository.findById(transaction.getSenderId()).orElse(null);
        if (sender == null) {
            throw new IllegalArgumentException("Sender account not found.");
        }

        User receiver = userRepository.findById(transaction.getReceiverId()).orElse(null);
        if (receiver == null) {
            throw new IllegalArgumentException("Recipient account not found.");
        }

        if (sender.getBalance() < transaction.getAmount()) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        // Calculate dynamic behavioral metrics
        double avgTxAmount = transactionRepository.getAverageTransactionAmountForUser(transaction.getSenderId());
        if (avgTxAmount == 0.0) {
            avgTxAmount = transaction.getAmount();
        }
        LocalDateTime past24Hours = LocalDateTime.now().minusDays(1);
        long frequency24h = transactionRepository.countBySenderIdAndTimestampAfter(transaction.getSenderId(), past24Hours);

        request.setAvgTransaction(avgTxAmount);
        request.setTransactionFrequency((int) (frequency24h + 1));

        // ✅ Timestamp
        transaction.setTimestamp(
                LocalDateTime.now()
        );

        assignCoordinatesIfEmpty(transaction, request);

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

        // 🚨 BLOCKED (If AI confidence is high and strong signals of fraud are detected)
        boolean strongFraudSignals =
                request.getDeviceChange() == 1
                        ||
                        request.getLocationRisk() == 1
                        ||
                        request.getTransactionFrequency() > 10;

        if (confidence >= 90 && strongFraudSignals) {
            transaction.setStatus("BLOCKED");
            transaction = transactionRepository.save(transaction);

            Map<String, Object> alert = new HashMap<>();
            alert.put("id", transaction.getId());
            alert.put("senderId", transaction.getSenderId());
            alert.put("receiverId", transaction.getReceiverId());
            alert.put("amount", transaction.getAmount());
            alert.put("status", transaction.getStatus());
            alert.put("confidence", confidence);
            alert.put("reasons", reasons);
            alert.put("latitude", transaction.getLatitude());
            alert.put("longitude", transaction.getLongitude());
            alert.put("message", "🚨 AI Fraud Detected!\n\n"
                    + "Amount: ₹" + transaction.getAmount()
                    + "\n\nConfidence: " + confidence + "%"
                    + "\n\nReasons: " + reasons);
            try {
                messagingTemplate.convertAndSend(
                        "/topic/fraud-alerts",
                        objectMapper.writeValueAsString(alert)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // ⚠️ SUSPICIOUS / SAFE (All other transactions now require OTP validation for safety)
        else {
            transaction.setStatus("SUSPICIOUS");
            transaction = transactionRepository.save(transaction);

            // Generate and send OTP via email
            String otp = otpService.generateOtp(
                    transaction.getId(),
                    transaction.getSenderId()
            );

            System.out.println("🔐 OTP FOR TRANSACTION " + transaction.getId() + ": " + otp);

            Map<String, Object> alert = new HashMap<>();
            alert.put("id", transaction.getId());
            alert.put("senderId", transaction.getSenderId());
            alert.put("receiverId", transaction.getReceiverId());
            alert.put("amount", transaction.getAmount());
            alert.put("status", transaction.getStatus());
            alert.put("confidence", confidence);
            alert.put("reasons", reasons);
            alert.put("latitude", transaction.getLatitude());
            alert.put("longitude", transaction.getLongitude());

            String alertMessage;
            if (confidence < 40) {
                alertMessage = "⚠️ Secure Transaction: OTP Sent to Email\n\n"
                        + "Amount: ₹" + transaction.getAmount()
                        + "\n\nConfidence: " + confidence + "%";
            } else {
                alertMessage = "⚠️ Suspicious Transaction: OTP Sent to Email\n\n"
                        + "Amount: ₹" + transaction.getAmount()
                        + "\n\nConfidence: " + confidence + "%"
                        + "\n\nReasons: " + reasons;
            }
            alert.put("message", alertMessage);

            try {
                messagingTemplate.convertAndSend(
                        "/topic/fraud-alerts",
                        objectMapper.writeValueAsString(alert)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return transaction;
    }
}
