package com.fraud.fraud_detection.security;

import com.fraud.fraud_detection.model.User;
import com.fraud.fraud_detection.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed Admin Account
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("Administrator");
            admin.setEmail("admin@fraud.com");
            admin.setBalance(1000000.0);
            admin.setStatus("ACTIVE");
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            System.out.println("🌱 Database seeded: admin account created!");
        }

        // Seed Default User Account
        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setName("Demo User");
            user.setEmail("chaitanyamhatre0805c@gmail.com");
            user.setBalance(500000.0);
            user.setStatus("ACTIVE");
            user.setRole("ROLE_USER");
            userRepository.save(user);
            System.out.println("🌱 Database seeded: user account created!");
        }
    }
}
