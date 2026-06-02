package com.fraud.fraud_detection.service;

import com.fraud.fraud_detection.dto.FraudAnalysisRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiFraudService {

    @Value("${ai.fraud.api.url}")
    private String aiApiUrl;

    public Map<String, Object> analyzeTransaction(
            FraudAnalysisRequest requestData
    ) {

        try {

            RestTemplate restTemplate =
                    new RestTemplate();

            Map<String, Object> request =
                    new HashMap<>();

            request.put(
                    "amount",
                    requestData.getAmount()
            );

            request.put(
                    "transaction_frequency",
                    requestData.getTransactionFrequency()
            );

            request.put(
                    "avg_transaction",
                    requestData.getAvgTransaction()
            );

            request.put(
                    "device_change",
                    requestData.getDeviceChange()
            );

            request.put(
                    "location_risk",
                    requestData.getLocationRisk()
            );

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(request, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            aiApiUrl,
                            entity,
                            Map.class
                    );

            Map<String, Object> aiResponse =
                    response.getBody();

            System.out.println(
                    "🤖 AI INPUT: " + request
            );

            System.out.println(
                    "🧠 AI RESPONSE: " + aiResponse
            );

            return aiResponse;

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }
}