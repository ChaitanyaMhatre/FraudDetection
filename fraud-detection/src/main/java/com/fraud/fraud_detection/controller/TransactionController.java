package com.fraud.fraud_detection.controller;

import com.fraud.fraud_detection.dto.FraudAnalysisRequest;
import com.fraud.fraud_detection.model.Transaction;
import com.fraud.fraud_detection.repository.TransactionRepository;
import com.fraud.fraud_detection.service.TransactionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @PostMapping("/send")
    public Transaction sendMoney(
            @RequestBody
            FraudAnalysisRequest request
    ) {

        Transaction transaction =
                new Transaction();

        transaction.setSenderId(
                request.getSenderId()
        );

        transaction.setReceiverId(
                request.getReceiverId()
        );

        transaction.setAmount(
                request.getAmount()
        );

        return transactionService.processTransaction(
                transaction,
                request
        );
    }

    @GetMapping("/all")
    public List<Transaction> getAllTransactions() {

        return transactionRepository.findAll();
    }
}