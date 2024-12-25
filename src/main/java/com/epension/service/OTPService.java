package com.epension.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OTPService {
    private final Map<String, String> otpStore = new HashMap<>();
    private final Random random = new Random();

    public void generateAndSendOTP(String phoneNumber) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpStore.put(phoneNumber, otp);
        // In a real application, you would send this OTP via SMS
        System.out.println("OTP for " + phoneNumber + ": " + otp);
    }

    public boolean verifyOTP(String phoneNumber, String otp) {
        String storedOTP = otpStore.get(phoneNumber);
        if (storedOTP != null && storedOTP.equals(otp)) {
            otpStore.remove(phoneNumber); // Remove OTP after successful verification
            return true;
        }
        return false;
    }
}
