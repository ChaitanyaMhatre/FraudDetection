package com.fraud.fraud_detection.dto;

public class FraudAnalysisRequest {

    private Long senderId;

    private Long receiverId;

    private double amount;

    private int transactionFrequency;

    private double avgTransaction;

    private int deviceChange;

    private int locationRisk;

    // ✅ Getters & Setters

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getTransactionFrequency() {
        return transactionFrequency;
    }

    public void setTransactionFrequency(
            int transactionFrequency
    ) {
        this.transactionFrequency =
                transactionFrequency;
    }

    public double getAvgTransaction() {
        return avgTransaction;
    }

    public void setAvgTransaction(
            double avgTransaction
    ) {
        this.avgTransaction = avgTransaction;
    }

    public int getDeviceChange() {
        return deviceChange;
    }

    public void setDeviceChange(
            int deviceChange
    ) {
        this.deviceChange = deviceChange;
    }

    public int getLocationRisk() {
        return locationRisk;
    }

    public void setLocationRisk(
            int locationRisk
    ) {
        this.locationRisk = locationRisk;
    }
}