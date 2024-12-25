package com.epension.controller;

import com.epension.model.User;
import com.epension.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            logger.info("Found {} users in database", users.size());
            users.forEach(user -> logger.info("User: {} - Password Hash: {}", user.getEmail(), user.getPassword()));
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Found " + users.size() + " users",
                "users", users
            ));
        } catch (Exception e) {
            logger.error("Error fetching users", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
                ));
        }
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<?> getUserByPhone(@PathVariable String phoneNumber) {
        try {
            return userRepository.findByPhoneNumber(phoneNumber)
                .map(user -> ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User found",
                    "user", user
                )))
                .orElse(ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No user found with phone: " + phoneNumber
                )));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
                ));
        }
    }
}
