package com.epension.controller;

import com.epension.model.Payment;
import com.epension.model.User;
import com.epension.repository.PaymentRepository;
import com.epension.repository.PensionApplicationRepository;
import com.epension.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PensionApplicationRepository pensionApplicationRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/history/{applicationId}")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Long applicationId, Authentication auth) {
        try {
            User user = userService.findByPhoneNumber(auth.getName());
            if (user == null) {
                logger.warn("User not found for phone: {}", auth.getName());
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }

            return pensionApplicationRepository.findById(applicationId)
                .map(application -> {
                    // Verify ownership
                    if (!application.getUser().getId().equals(user.getId())) {
                        logger.warn("Access denied for user {} to application {}", user.getPhoneNumber(), applicationId);
                        return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
                    }

                    List<Payment> payments = paymentRepository.findByPensionApplication(application);
                    logger.debug("Found {} payments for application {}", payments.size(), applicationId);
                    return ResponseEntity.ok(payments);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching payment history for application {}: {}", applicationId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody Payment payment, Authentication auth) {
        try {
            User user = userService.findByPhoneNumber(auth.getName());
            if (user == null) {
                logger.warn("User not found for phone: {}", auth.getName());
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }

            return pensionApplicationRepository.findById(payment.getPensionApplication().getId())
                .map(application -> {
                    // Verify ownership or admin role
                    if (!application.getUser().getId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
                        logger.warn("Access denied for user {} to process payment for application {}", 
                            user.getPhoneNumber(), application.getId());
                        return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
                    }

                    payment.setPensionApplication(application);
                    Payment savedPayment = paymentRepository.save(payment);
                    logger.info("Payment processed successfully for application {}", application.getId());
                    return ResponseEntity.ok(savedPayment);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPayment(@PathVariable Long id, Authentication auth) {
        try {
            User user = userService.findByPhoneNumber(auth.getName());
            if (user == null) {
                logger.warn("User not found for phone: {}", auth.getName());
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }

            return paymentRepository.findById(id)
                .map(payment -> {
                    // Verify ownership or admin role
                    if (!payment.getPensionApplication().getUser().getId().equals(user.getId()) 
                            && !"ADMIN".equals(user.getRole())) {
                        logger.warn("Access denied for user {} to view payment {}", user.getPhoneNumber(), id);
                        return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
                    }
                    return ResponseEntity.ok(payment);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching payment {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
