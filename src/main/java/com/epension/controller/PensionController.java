package com.epension.controller;

import com.epension.model.PensionApplication;
import com.epension.model.User;
import com.epension.repository.PensionApplicationRepository;
import com.epension.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/pension")
@CrossOrigin(origins = "*")
public class PensionController {

    private static final Logger logger = LoggerFactory.getLogger(PensionController.class);

    @Autowired
    private PensionApplicationRepository pensionApplicationRepository;

    @Autowired
    private UserService userService;

    @Value("${app.file.upload-dir:uploads/documents}")
    private String uploadDir;

    private Path getFileStoragePath() {
        return Paths.get(uploadDir);
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForPension(
            @RequestPart("application") PensionApplication application,
            @RequestPart("aadharCard") MultipartFile aadharCard,
            @RequestPart("panCard") MultipartFile panCard,
            @RequestPart("bankPassbook") MultipartFile bankPassbook,
            @RequestPart("retirementProof") MultipartFile retirementProof,
            @RequestPart("photograph") MultipartFile photograph,
            Authentication auth) {
        
        try {
            logger.debug("Processing pension application for user: {}", auth.getName());

            User user = userService.findByPhoneNumber(auth.getName());
            if (user == null) {
                logger.warn("User not found for phone: {}", auth.getName());
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }

            // Generate application number
            String applicationNumber = generateApplicationNumber();
            application.setApplicationNumber(applicationNumber);

            // Set dates
            application.setAppliedDate(LocalDate.now());
            application.setLastUpdated(LocalDate.now());

            // Save documents
            application.setAadharCardDoc(saveDocument(aadharCard));
            application.setPanCardDoc(saveDocument(panCard));
            application.setBankPassbookDoc(saveDocument(bankPassbook));
            application.setRetirementProofDoc(saveDocument(retirementProof));
            application.setPhotographDoc(saveDocument(photograph));

            // Set user and status
            application.setUser(user);
            application.setStatus("PENDING");

            // Validate mandatory fields
            validateApplication(application);

            // Save application
            PensionApplication savedApplication = pensionApplicationRepository.save(application);
            logger.info("Pension application saved successfully for user: {}", user.getPhoneNumber());
            return ResponseEntity.ok(savedApplication);
        } catch (Exception e) {
            logger.error("Error processing pension application: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/applications")
    public ResponseEntity<List<PensionApplication>> getApplications(Authentication auth) {
        try {
            User user = userService.findByPhoneNumber(auth.getName());
            if (user == null) {
                logger.warn("User not found for phone: {}", auth.getName());
                return ResponseEntity.badRequest().build();
            }
            
            List<PensionApplication> applications = pensionApplicationRepository.findByUser(user);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            logger.error("Error fetching applications: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/application/{id}")
    public ResponseEntity<?> getApplication(@PathVariable Long id, Authentication auth) {
        try {
            return pensionApplicationRepository.findById(id)
                .map(application -> {
                    User user = userService.findByPhoneNumber(auth.getName());
                    if (user == null) {
                        logger.warn("User not found for phone: {}", auth.getName());
                        return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
                    }
                    
                    if (!application.getUser().getId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
                        logger.warn("Access denied for user {} to application {}", user.getPhoneNumber(), id);
                        return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
                    }
                    return ResponseEntity.ok(application);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching application {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/application/{id}")
    public ResponseEntity<?> updateApplication(@PathVariable Long id, @RequestBody PensionApplication application) {
        try {
            return pensionApplicationRepository.findById(id)
                .map(existing -> {
                    existing.setStatus(application.getStatus());
                    existing.setMonthlyAmount(application.getMonthlyAmount());
                    existing.setApprovalDate(application.getApprovalDate());
                    existing.setNextPaymentDate(application.getNextPaymentDate());
                    existing.setRemarks(application.getRemarks());
                    existing.setLastUpdated(LocalDate.now());

                    PensionApplication updated = pensionApplicationRepository.save(existing);
                    logger.info("Application {} updated successfully", id);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error updating application {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private String generateApplicationNumber() {
        return "PEN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String saveDocument(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Create directories if they don't exist
        Path fileStoragePath = getFileStoragePath();
        Files.createDirectories(fileStoragePath);

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path targetPath = fileStoragePath.resolve(filename);

        // Save file
        Files.copy(file.getInputStream(), targetPath);
        logger.debug("Document saved successfully: {}", filename);

        return filename;
    }

    private void validateApplication(PensionApplication application) {
        // Personal Information
        if (application.getAadharNumber() == null || application.getAadharNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Aadhar number is required");
        }
        if (application.getPanNumber() == null || application.getPanNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("PAN number is required");
        }
        if (application.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }
        if (application.getGender() == null || application.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("Gender is required");
        }
        if (application.getMaritalStatus() == null || application.getMaritalStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Marital status is required");
        }

        // Contact Information
        if (application.getPermanentAddress() == null || application.getPermanentAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Permanent address is required");
        }
        if (application.getCity() == null || application.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
        if (application.getState() == null || application.getState().trim().isEmpty()) {
            throw new IllegalArgumentException("State is required");
        }
        if (application.getPinCode() == null || application.getPinCode().trim().isEmpty()) {
            throw new IllegalArgumentException("PIN code is required");
        }

        // Employment Details
        if (application.getLastEmployer() == null || application.getLastEmployer().trim().isEmpty()) {
            throw new IllegalArgumentException("Last employer is required");
        }
        if (application.getRetirementDate() == null) {
            throw new IllegalArgumentException("Retirement date is required");
        }
        if (application.getEmployeeId() == null || application.getEmployeeId().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        if (application.getLastDrawnSalary() == null || application.getLastDrawnSalary() <= 0) {
            throw new IllegalArgumentException("Last drawn salary is required and must be greater than 0");
        }

        // Bank Details
        if (application.getBankName() == null || application.getBankName().trim().isEmpty()) {
            throw new IllegalArgumentException("Bank name is required");
        }
        if (application.getAccountNumber() == null || application.getAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Account number is required");
        }
        if (application.getIfscCode() == null || application.getIfscCode().trim().isEmpty()) {
            throw new IllegalArgumentException("IFSC code is required");
        }

        // Pension Type
        if (application.getPensionType() == null || application.getPensionType().trim().isEmpty()) {
            throw new IllegalArgumentException("Pension type is required");
        }

        // Declaration
        if (!application.isDeclarationAccepted()) {
            throw new IllegalArgumentException("Please accept the declaration");
        }
    }
}
