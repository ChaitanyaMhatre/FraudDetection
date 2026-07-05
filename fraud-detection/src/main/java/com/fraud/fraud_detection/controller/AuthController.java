package com.fraud.fraud_detection.controller;

import com.fraud.fraud_detection.model.User;
import com.fraud.fraud_detection.repository.UserRepository;
import com.fraud.fraud_detection.security.JwtTokenProvider;
import com.fraud.fraud_detection.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found in system."));
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please verify your email address first. Check your inbox for the verification link."));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        String token = tokenProvider.generateToken(username, user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole().replace("ROLE_", "")); // frontend uses USER/ADMIN directly
        response.put("name", user.getName());
        response.put("balance", user.getBalance());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> registerRequest) {
        String username = registerRequest.get("username");
        String password = registerRequest.get("password");
        String name = registerRequest.get("name");
        String email = registerRequest.get("email");

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is already taken!"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setEmail(email);
        user.setBalance(50000.0); // Default balance for new accounts
        user.setStatus("PENDING_VERIFICATION");
        user.setRole("ROLE_USER");

        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);

        userRepository.save(user);

        try {
            emailService.sendVerificationEmail(email, token);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Failed to send verification email: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "User registered successfully! A verification email has been sent. Please verify before logging in."));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        User user = userRepository.findByEmailVerificationToken(token).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest()
                    .header("Content-Type", "text/html")
                    .body("<html><body style='font-family: Arial, sans-serif; text-align: center; margin-top: 100px; background-color: #f8f9fa;'>"
                            + "<div style='display: inline-block; padding: 40px; border-radius: 8px; background: white; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>"
                            + "<h1 style='color: #dc3545;'>❌ Invalid or Expired Verification Link</h1>"
                            + "<p style='font-size: 16px; color: #6c757d; margin-top: 20px;'>The email verification link you clicked is invalid or has expired.</p>"
                            + "</div></body></html>");
        }

        user.setStatus("ACTIVE");
        user.setEmailVerificationToken(null);
        userRepository.save(user);

        return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body("<html><body style='font-family: Arial, sans-serif; text-align: center; margin-top: 100px; background-color: #f8f9fa;'>"
                        + "<div style='display: inline-block; padding: 40px; border-radius: 8px; background: white; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>"
                        + "<h1 style='color: #28a745;'>✅ Email Verified Successfully!</h1>"
                        + "<p style='font-size: 16px; color: #6c757d; margin-top: 20px;'>Your account is now active. You can now close this window and log in to the ePay dashboard.</p>"
                        + "</div></body></html>");
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("balance", user.getBalance());
        return ResponseEntity.ok(response);
    }
}
