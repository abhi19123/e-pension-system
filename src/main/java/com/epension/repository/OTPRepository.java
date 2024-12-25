package com.epension.repository;

import com.epension.model.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTP, Long> {
    Optional<OTP> findByPhoneNumberAndOtpAndUsedFalse(String phoneNumber, String otp);
    Optional<OTP> findTopByPhoneNumberOrderByExpiryTimeDesc(String phoneNumber);
}
