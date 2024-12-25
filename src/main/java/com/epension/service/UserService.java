package com.epension.service;

import com.epension.model.User;
import com.epension.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    public User findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
            .orElse(null);
    }

    public long countPensioners() {
        return userRepository.findByRole("PENSIONER").size();
    }
}
