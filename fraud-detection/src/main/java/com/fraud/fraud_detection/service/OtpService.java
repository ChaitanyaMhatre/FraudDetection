package com.fraud.fraud_detection.service;

import com.fraud.fraud_detection.model.OtpVerification;
import com.fraud.fraud_detection.repository.OtpVerificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

import com.fraud.fraud_detection.model.User;
import com.fraud.fraud_detection.repository.UserRepository;

@Service
@Transactional
public class OtpService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpVerificationRepository
            otpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🔥 Generate OTP
    public String generateOtp(
            Long transactionId,
            Long senderId
    ) {

        Random random = new Random();

        String otpRaw = String.valueOf(
                100000
                        + random.nextInt(900000)
        );

        OtpVerification verification =
                new OtpVerification();

        verification.setTransactionId(
                transactionId
        );

        // Store secure hashed value of OTP in the database
        verification.setOtp(passwordEncoder.encode(otpRaw));

        verification.setVerified(false);

        verification.setCreatedAt(
                LocalDateTime.now()
        );

        otpRepository.save(verification);

        System.out.println(
                "🔐 GENERATED OTP (PLAIN): "
                        + otpRaw
        );

        User user =
                userRepository.findById(
                        senderId
                ).orElse(null);

        if (user != null) {
            emailService.sendOtpEmail(
                    user.getEmail(),
                    otpRaw
            );
        }
        return otpRaw;
    }

    // ✅ Verify OTP
    public boolean verifyOtp(
            Long transactionId,
            String otpRaw
    ) {

        OtpVerification verification =
                otpRepository.findByTransactionId(
                        transactionId
                );

        if (verification == null || verification.isVerified()) {
            return false;
        }

        // Increment attempts count and save
        int currentAttempts = verification.getAttempts() != null ? verification.getAttempts() : 0;
        verification.setAttempts(currentAttempts + 1);
        otpRepository.save(verification);

        // Check attempt limits: Max 3 attempts
        if (verification.getAttempts() > 3) {
            System.err.println("❌ OTP Verification failed: Maximum attempts exceeded for transaction " + transactionId);
            return false;
        }

        // Check expiration: 5 minutes limit
        if (verification.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
            System.err.println("❌ OTP Verification failed: OTP has expired for transaction " + transactionId);
            return false;
        }

        // Check OTP match
        if (passwordEncoder.matches(otpRaw, verification.getOtp())) {
            verification.setVerified(true);
            otpRepository.save(verification);
            return true;
        }

        return false;
    }
}