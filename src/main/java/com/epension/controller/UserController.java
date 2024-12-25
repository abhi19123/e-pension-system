package com.epension.controller;

import com.epension.model.User;
import com.epension.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:8081"})
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<?> getUserByPhone(@PathVariable String phoneNumber) {
        logger.info("Looking up user by phone number: {}", phoneNumber);
        try {
            return userRepository.findByPhoneNumber(phoneNumber)
                    .map(user -> {
                        logger.info("Found user: {}", user.getEmail());
                        return ResponseEntity.ok(user);
                    })
                    .orElseGet(() -> {
                        logger.error("No user found with phone number: {}", phoneNumber);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("Error looking up user by phone number: {}", phoneNumber, e);
            return ResponseEntity.internalServerError().body("Error finding user: " + e.getMessage());
        }
    }

    // Test endpoint to check all users
    @GetMapping("/test/all")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            logger.info("Found {} users in database", users.size());
            users.forEach(user -> logger.info("User: {} - Phone: {}", user.getEmail(), user.getPhoneNumber()));
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching all users", e);
            return ResponseEntity.internalServerError().body("Error fetching users: " + e.getMessage());
        }
    }
}
