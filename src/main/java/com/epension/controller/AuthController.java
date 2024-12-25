package com.epension.controller;

import com.epension.dto.LoginRequest;
import com.epension.dto.SignupRequest;
import com.epension.dto.UserDto;
import com.epension.model.User;
import com.epension.repository.UserRepository;
import com.epension.service.AuthService;
import com.epension.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = { "http://localhost:8080",
        "http://localhost:8081" }, allowCredentials = "true", allowedHeaders = "*", methods = { RequestMethod.GET,
                RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS })
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            logger.debug("Login attempt for phone: {}, role: {}", request.getPhoneNumber(), request.getRole());

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            User user = authService.findByPhoneNumber(request.getPhoneNumber());
            if (user == null) {
                logger.warn("No user found for phone: {}", request.getPhoneNumber());
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }

            // Verify role
            String expectedRole = "ROLE_" + request.getRole();
            if (!user.getRole().equals(expectedRole)) {
                logger.warn("Role mismatch for user {}: expected {}, got {}",
                        user.getPhoneNumber(), user.getRole(), expectedRole);
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid role for user"));
            }

            // Generate JWT token
            String token = jwtService.generateToken(user);
            logger.debug("Generated token for user: {}", user.getPhoneNumber());

            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", Map.of(
                    "phoneNumber", user.getPhoneNumber(),
                    "fullName", user.getFullName(),
                    "role", user.getRole(),
                    "email", user.getEmail()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login error for phone: " + request.getPhoneNumber(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid credentials"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest request) {
        try {
            logger.debug("Registration attempt for phone: {}, role: {}", request.getPhoneNumber(), request.getRole());

            // Clean phone number
            String phoneNumber = request.getPhoneNumber().trim()
                    .replaceAll("\\s+", "")
                    .replaceAll("^\\+91", "");

            // Check if email exists
            if (userRepository.existsByEmail(request.getEmail())) {
                logger.warn("Email already exists: {}", request.getEmail());
                return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
            }

            // Check if phone number exists
            if (userRepository.existsByPhoneNumber(phoneNumber)) {
                logger.warn("Phone number already exists: {}", phoneNumber);
                return ResponseEntity.badRequest().body(Map.of("message", "Phone number already registered"));
            }

            UserDto user = authService.registerUser(request);
            logger.info("Registration successful for user: {}", user.getEmail());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Registration successful",
                    "user", user));
        } catch (Exception e) {
            logger.error("Registration error for phone: " + request.getPhoneNumber(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestParam String phoneNumber, @RequestParam String otp) {
        try {
            return ResponseEntity.ok(authService.verifyOTP(phoneNumber, otp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOTP(@RequestParam String phoneNumber) {
        try {
            authService.sendOTP(phoneNumber);
            return ResponseEntity.ok("OTP sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUser(@RequestParam String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return ResponseEntity.ok(UserDto.fromUser(user.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody String idToken) {
        try {
            return ResponseEntity.ok(authService.authenticateWithGoogle(idToken));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/test-user")
    public ResponseEntity<?> testUser(@RequestParam String phoneNumber) {
        try {
            // Try with original phone number
            Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);

            // Try with +91 prefix
            if (user.isEmpty() && !phoneNumber.startsWith("+91")) {
                user = userRepository.findByPhoneNumber("+91" + phoneNumber);
            }

            // Try without +91 prefix
            if (user.isEmpty() && phoneNumber.startsWith("+91")) {
                user = userRepository.findByPhoneNumber(phoneNumber.substring(3));
            }

            if (user.isPresent()) {
                User foundUser = user.get();
                return ResponseEntity.ok(Map.of(
                        "found", true,
                        "phoneNumber", foundUser.getPhoneNumber(),
                        "role", foundUser.getRole(),
                        "fullName", foundUser.getFullName()));
            }

            return ResponseEntity.ok(Map.of(
                    "found", false,
                    "message", "User not found",
                    "searchedPhone", phoneNumber));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/check-user")
    public ResponseEntity<?> checkUserDetails(@RequestParam String phoneNumber) {
        try {
            // Clean and try different phone number formats
            User user = null;
            String cleanPhone = phoneNumber.trim()
                    .replaceAll("\\s+", "")
                    .replaceAll("^\\+91", ""); // Remove +91 prefix like in registration

            // Try without prefix first (as stored during registration)
            user = authService.findByPhoneNumber(cleanPhone);

            if (user == null) {
                // Try with +91 prefix
                user = authService.findByPhoneNumber("+91" + cleanPhone);
            }

            if (user != null) {
                logger.info("User found with phone: {}, stored as: {}", phoneNumber, user.getPhoneNumber());
                return ResponseEntity.ok(Map.of(
                        "exists", true,
                        "storedPhone", user.getPhoneNumber(),
                        "storedRole", user.getRole(),
                        "hasPassword", user.getPassword() != null,
                        "searchedWith", phoneNumber));
            }

            logger.warn("No user found for phone: {} (cleaned: {})", phoneNumber, cleanPhone);
            return ResponseEntity.ok(Map.of(
                    "exists", false,
                    "searchedWith", phoneNumber,
                    "cleanedPhone", cleanPhone,
                    "message", "No user found with this phone number"));
        } catch (Exception e) {
            logger.error("Error checking user details for phone: " + phoneNumber, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
