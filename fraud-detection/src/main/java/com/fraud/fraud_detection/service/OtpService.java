package com.fraud.fraud_detection.service;

import com.fraud.fraud_detection.model.OtpVerification;
import com.fraud.fraud_detection.repository.OtpVerificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

import com.fraud.fraud_detection.model.User;
import com.fraud.fraud_detection.repository.UserRepository;

@Service
public class OtpService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpVerificationRepository
            otpRepository;

    // 🔥 Generate OTP
    public String generateOtp(
            Long transactionId,
            Long senderId
    ) {

        Random random = new Random();

        String otp = String.valueOf(

                100000
                        + random.nextInt(900000)
        );

        OtpVerification verification =
                new OtpVerification();

        verification.setTransactionId(
                transactionId
        );

        verification.setOtp(otp);

        verification.setVerified(false);

        verification.setCreatedAt(
                LocalDateTime.now()
        );

        otpRepository.save(verification);

        System.out.println(
                "🔐 GENERATED OTP: "
                        + otp
        );

        User user =
                userRepository.findById(
                        senderId
                ).orElse(null);

        if (user != null) {

            emailService.sendOtpEmail(

                    user.getEmail(),

                    otp
            );
        }
        return otp;
    }

    // ✅ Verify OTP
    public boolean verifyOtp(
            Long transactionId,
            String otp
    ) {

        OtpVerification verification =
                otpRepository.findByTransactionId(
                        transactionId
                );

        if (
                verification != null
                        &&
                        verification.getOtp()
                                .equals(otp)
        ) {

            verification.setVerified(true);

            otpRepository.save(
                    verification
            );

            return true;
        }

        return false;
    }
}