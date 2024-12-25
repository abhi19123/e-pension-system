package com.epension.service;

import org.springframework.stereotype.Service;

@Service
public class SMSService {
    public void sendSMS(String to, String message) {
        // SMS functionality temporarily disabled
        System.out.println("SMS would be sent to: " + to);
        System.out.println("Message: " + message);
    }
}
