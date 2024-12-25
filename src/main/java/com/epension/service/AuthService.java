package com.epension.service;

import com.epension.dto.AuthResponse;
import com.epension.dto.LoginRequest;
import com.epension.dto.SignupRequest;
import com.epension.dto.UserDto;
import com.epension.model.User;
import com.epension.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OTPService otpService;

    @Value("${google.client.id:dummy-id}")
    private String googleClientId;

    public User findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
            .orElse(null);
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("=== Starting login process ===");
        logger.info("Raw login request - phone: '{}', role: '{}', password length: {}", 
            request.getPhoneNumber(), 
            request.getRole(),
            request.getPassword() != null ? request.getPassword().length() : 0);
        
        try {
            // Validate request
            if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
                logger.error("Phone number is required");
                return AuthResponse.builder()
                    .success(false)
                    .message("Phone number is required")
                    .build();
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                logger.error("Password is required");
                return AuthResponse.builder()
                    .success(false)
                    .message("Password is required")
                    .build();
            }

            if (request.getRole() == null || request.getRole().trim().isEmpty()) {
                logger.error("Role is required");
                return AuthResponse.builder()
                    .success(false)
                    .message("Role is required")
                    .build();
            }

            // Clean and format phone number - ONLY use the format without +91
            String phoneNumber = request.getPhoneNumber().trim()
                .replaceAll("\\s+", "")
                .replaceAll("^\\+91", "");  // Remove +91 if present
            
            logger.info("Searching for phone number in DB: '{}'", phoneNumber);

            // Search for user
            Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);
            logger.info("Search result: {}", userOpt.isPresent());
            
            if (userOpt.isEmpty()) {
                logger.error("No user found with phone number: '{}'", phoneNumber);
                // Log all users for debugging
                List<User> allUsers = userRepository.findAll();
                logger.info("Current users in database:");
                for (User u : allUsers) {
                    logger.info("User: {}, Phone: '{}', Role: '{}'", u.getFullName(), u.getPhoneNumber(), u.getRole());
                }
                return AuthResponse.builder()
                    .success(false)
                    .message("Invalid credentials")
                    .build();
            }

            User user = userOpt.get();
            logger.info("Found user: {}, phone: '{}', role: '{}'", user.getFullName(), user.getPhoneNumber(), user.getRole());

            // Validate role (case-insensitive)
            String expectedRole = "ROLE_" + request.getRole().toUpperCase();
            if (!user.getRole().equalsIgnoreCase(expectedRole)) {
                logger.error("Role mismatch. Expected: '{}', Found: '{}'", expectedRole, user.getRole());
                return AuthResponse.builder()
                    .success(false)
                    .message("Invalid credentials")
                    .build();
            }

            // Check password
            boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
            logger.info("Password validation result: {}", passwordMatches);

            if (!passwordMatches) {
                logger.error("Password validation failed for user: {}", user.getFullName());
                return AuthResponse.builder()
                    .success(false)
                    .message("Invalid credentials")
                    .build();
            }

            // Generate token
            String token = jwtService.generateToken(user);
            logger.info("Login successful for user: {}", user.getFullName());

            return AuthResponse.builder()
                .success(true)
                .token(token)
                .user(UserDto.fromUser(user))
                .message("Login successful")
                .build();

        } catch (Exception e) {
            logger.error("Login failed with exception", e);
            return AuthResponse.builder()
                .success(false)
                .message("An unexpected error occurred")
                .build();
        }
    }

    public UserDto registerUser(SignupRequest request) {
        logger.info("Starting user registration for email: {}, phone: {}", request.getEmail(), request.getPhoneNumber());
        try {
            // Validate phone number format
            String phoneNumber = request.getPhoneNumber().trim()
                .replaceAll("\\s+", "")
                .replaceAll("^\\+91", "");  // Remove +91 if present
            
            // Additional validation
            if (phoneNumber.length() != 10) {
                logger.error("Invalid phone number length: {}", phoneNumber);
                throw new RuntimeException("Phone number must be exactly 10 digits");
            }

            if (!phoneNumber.matches("^[0-9]{10}$")) {
                logger.error("Phone number contains invalid characters: {}", phoneNumber);
                throw new RuntimeException("Phone number must contain only digits");
            }

            // Check if user already exists
            if (userRepository.existsByPhoneNumber(phoneNumber)) {
                logger.error("Phone number already exists: {}", phoneNumber);
                throw new RuntimeException("Phone number is already registered");
            }

            if (userRepository.existsByEmail(request.getEmail())) {
                logger.error("Email already exists: {}", request.getEmail());
                throw new RuntimeException("Email is already registered");
            }

            // Create new user
            User user = new User();
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhoneNumber(phoneNumber);  // Store without +91
            user.setRole("ROLE_" + request.getRole().toUpperCase());  // Add ROLE_ prefix and ensure uppercase
            
            // Set optional fields
            if (request.getEmployeeId() != null) {
                user.setEmployeeId(request.getEmployeeId());
            }
            if (request.getDepartment() != null) {
                user.setDepartment(request.getDepartment());
            }
            if (request.getAadharNumber() != null) {
                if (!request.getAadharNumber().matches("^[0-9]{12}$")) {
                    logger.error("Invalid Aadhar number format: {}", request.getAadharNumber());
                    throw new RuntimeException("Aadhar number must be exactly 12 digits");
                }
                user.setAadharNumber(request.getAadharNumber());
            }
            
            // Parse dates if provided
            try {
                if (request.getDateOfBirth() != null && !request.getDateOfBirth().isEmpty()) {
                    user.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
                }
                if (request.getRetirementDate() != null && !request.getRetirementDate().isEmpty()) {
                    user.setRetirementDate(LocalDate.parse(request.getRetirementDate()));
                }
            } catch (Exception e) {
                logger.error("Date parsing error: {}", e.getMessage());
                throw new RuntimeException("Invalid date format. Please use YYYY-MM-DD format");
            }
            
            if (request.getPensionType() != null) {
                user.setPensionType(request.getPensionType().toUpperCase());
            }
            
            user.setEmailVerified(false);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            // Save user
            logger.info("Attempting to save user: {}", user);
            User savedUser = userRepository.save(user);
            logger.info("User registered successfully: {}", savedUser.getEmail());
            return UserDto.fromUser(savedUser);
            
        } catch (Exception e) {
            logger.error("Registration failed", e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    public AuthResponse authenticateWithGoogle(String idTokenString) {
        // For now, return error as Google auth is not fully configured
        return AuthResponse.builder()
                .success(false)
                .message("Google authentication not configured")
                .build();
    }

    public AuthResponse verifyOTP(String phoneNumber, String otp) {
        if (otpService.verifyOTP(phoneNumber, otp)) {
            Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String token = jwtService.generateToken(user);
                return AuthResponse.builder()
                        .success(true)
                        .token(token)
                        .user(UserDto.fromUser(user))
                        .build();
            }
        }
        return AuthResponse.builder()
                .success(false)
                .message("Invalid OTP")
                .build();
    }

    public void sendOTP(String phoneNumber) {
        otpService.generateAndSendOTP(phoneNumber);
    }
}
