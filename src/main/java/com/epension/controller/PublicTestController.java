package com.epension.controller;

import com.epension.model.User;
import com.epension.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/test")
@CrossOrigin(origins = "http://localhost:8081")
public class PublicTestController {
    private static final Logger logger = LoggerFactory.getLogger(PublicTestController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            logger.info("Found {} users in database", users.size());
            users.forEach(user -> {
                logger.info("User: {} - Phone: {} - Role: {}", 
                    user.getEmail(), 
                    user.getPhoneNumber(),
                    user.getRole());
            });
            return ResponseEntity.ok(Map.of(
                "message", "Found " + users.size() + " users",
                "users", users
            ));
        } catch (Exception e) {
            logger.error("Error fetching users", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error fetching users: " + e.getMessage()));
        }
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<?> testPhoneLookup(@PathVariable String phoneNumber) {
        try {
            return userRepository.findByPhoneNumber(phoneNumber)
                .map(user -> {
                    logger.info("Found user by phone {}: {} ({})", 
                        phoneNumber, user.getEmail(), user.getRole());
                    return ResponseEntity.ok(Map.of(
                        "message", "Found user",
                        "user", user
                    ));
                })
                .orElseGet(() -> {
                    logger.warn("No user found with phone: {}", phoneNumber);
                    return ResponseEntity.ok(Map.of(
                        "message", "No user found with phone: " + phoneNumber
                    ));
                });
        } catch (Exception e) {
            logger.error("Error looking up phone: " + phoneNumber, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error looking up phone: " + e.getMessage()));
        }
    }
}
