package com.fraud.fraud_detection.controller;

import com.fraud.fraud_detection.model.Transaction;
import com.fraud.fraud_detection.repository.TransactionRepository;
import com.fraud.fraud_detection.service.OtpService;
import com.fraud.fraud_detection.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@CrossOrigin(origins = "http://localhost:4200")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TransactionService transactionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, Object> request) {
        Long transactionId = Long.valueOf(request.get("transactionId").toString());
        String otp = request.get("otp").toString();

        boolean isVerified = otpService.verifyOtp(transactionId, otp);
        Map<String, Object> response = new HashMap<>();

        if (isVerified) {
            Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
            if (transaction != null && "SUSPICIOUS".equals(transaction.getStatus())) {
                transaction.setStatus("SUCCESS");
                transactionRepository.save(transaction);
                transactionService.updateBalances(transaction);

                // Broadcast via websocket
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", transaction.getId());
                alert.put("senderId", transaction.getSenderId());
                alert.put("receiverId", transaction.getReceiverId());
                alert.put("amount", transaction.getAmount());
                alert.put("status", transaction.getStatus());
                alert.put("confidence", 0.0); // Reset or static
                alert.put("reasons", new String[]{"OTP Verified Successfully"});
                alert.put("latitude", transaction.getLatitude());
                alert.put("longitude", transaction.getLongitude());
                alert.put("message", "✅ Transaction OTP Verified\n\n"
                        + "Transaction ID: " + transactionId
                        + "\nAmount: ₹" + transaction.getAmount()
                        + "\nStatus: SUCCESS");
                try {
                    messagingTemplate.convertAndSend(
                            "/topic/fraud-alerts",
                            objectMapper.writeValueAsString(alert)
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            response.put("success", true);
            response.put("message", "OTP verified and transaction completed!");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid OTP or OTP has expired.");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
