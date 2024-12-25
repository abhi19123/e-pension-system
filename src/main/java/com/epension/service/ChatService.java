package com.epension.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ChatService {
    private final Map<Pattern, String> responses;

    public ChatService() {
        responses = new HashMap<>();
        // Add common pension-related queries and responses
        responses.put(Pattern.compile("(?i).*how.*apply.*pension.*"), 
            "To apply for pension: 1. Log in to your account 2. Go to 'Apply for Pension' section 3. Fill out the application form 4. Upload required documents 5. Submit the application");
        
        responses.put(Pattern.compile("(?i).*document.*required.*"), 
            "Required documents include: 1. Proof of Identity (Aadhar/PAN) 2. Age Proof 3. Service Records 4. Bank Account Details 5. Recent Photograph");
        
        responses.put(Pattern.compile("(?i).*check.*status.*"), 
            "To check your pension status: 1. Log in to your account 2. Go to 'Track Application' 3. Enter your application number");
        
        responses.put(Pattern.compile("(?i).*payment.*delay.*"), 
            "If your payment is delayed: 1. Check your bank account details 2. Verify your life certificate status 3. Contact our support team");
        
        responses.put(Pattern.compile("(?i).*contact.*support.*"), 
            "You can contact support through: Email: support@epension.com Phone: 1800-XXX-XXXX Available Mon-Fri, 9 AM to 5 PM");
    }

    public String generateResponse(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "I couldn't understand your message. Please try again.";
        }

        for (Map.Entry<Pattern, String> entry : responses.entrySet()) {
            if (entry.getKey().matcher(userMessage).matches()) {
                return entry.getValue();
            }
        }

        return "I apologize, but I don't have specific information about that. Please contact our support team for more detailed assistance.";
    }
}
