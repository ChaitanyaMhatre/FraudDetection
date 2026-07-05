package com.fraud.fraud_detection.controller;

import com.fraud.fraud_detection.dto.FraudAnalysisRequest;
import com.fraud.fraud_detection.model.Transaction;
import com.fraud.fraud_detection.repository.TransactionRepository;
import com.fraud.fraud_detection.service.TransactionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @PostMapping("/send")
    public ResponseEntity<?> sendMoney(
            @RequestBody
            FraudAnalysisRequest request
    ) {
        try {
            Transaction transaction = new Transaction();
            transaction.setSenderId(request.getSenderId());
            transaction.setReceiverId(request.getReceiverId());
            transaction.setAmount(request.getAmount());
            transaction.setLatitude(request.getLatitude());
            transaction.setLongitude(request.getLongitude());

            Transaction result = transactionService.processTransaction(
                    transaction,
                    request
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    public List<Transaction> getAllTransactions() {

        return transactionRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<Transaction> getUserTransactions(@PathVariable Long userId) {
        return transactionRepository.findBySenderIdOrReceiverIdOrderByIdDesc(userId, userId);
    }
}