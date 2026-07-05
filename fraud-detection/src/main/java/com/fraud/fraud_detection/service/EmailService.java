package com.fraud.fraud_detection.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOtpEmail(

            String toEmail,

            String otp
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);

        message.setSubject(
                "Fraud Detection OTP"
        );

        message.setText(

                "Your OTP is: "
                        + otp
        );

        try {
            System.out.println("📧 [THREAD: " + Thread.currentThread().getName() + "] Sending OTP email to " + toEmail);
            mailSender.send(message);
            System.out.println(
                    "📧 OTP EMAIL SENT SUCCESS"
            );
        } catch (Exception e) {
            System.err.println("❌ Failed to send OTP email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to ePay - Please Verify Your Email");
        String verificationUrl = "http://localhost:8080/api/auth/verify-email?token=" + token;
        message.setText("Welcome to ePay!\n\n"
                + "Please click the link below to verify your email address and activate your account:\n"
                + verificationUrl + "\n\n"
                + "If you did not create an account on ePay, please ignore this email.\n\n"
                + "Regards,\nePay Security Team");
        try {
            mailSender.send(message);
            System.out.println("📧 VERIFICATION EMAIL SENT TO: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send verification email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Print the verification URL to console as a fallback
        System.out.println("\n=============================================================");
        System.out.println("🔗 VERIFICATION LINK (FALLBACK FOR TESTING):");
        System.out.println("   " + verificationUrl);
        System.out.println("=============================================================\n");
    }
}